import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  input,
  LOCALE_ID,
  signal,
} from '@angular/core';
import { DatePipe, DecimalPipe, formatDate } from '@angular/common';
import { PuntoProgresion } from '../../../core/entrenamiento/entrenamiento';

const ALTO = 220;
const MARGEN = { arriba: 12, derecha: 12, abajo: 28, izquierda: 44 };
const MS_POR_DIA = 86_400_000;

/**
 * Resumen y gráfico de la progresión de un ejercicio. Solo dibuja: recibe los puntos y no sabe
 * de quién son, así sirve igual para la progresión propia y para la de un amigo.
 *
 * El SVG se dibuja en píxeles reales (mide el ancho del contenedor con un ResizeObserver),
 * así los textos no se achican en el celular como pasaría escalando un viewBox fijo.
 */
@Component({
  selector: 'app-progresion',
  imports: [DatePipe, DecimalPipe],
  styleUrl: './progresion.css',
  templateUrl: './progresion.html',
})
export class Progresion {
  /** Ordenados del entrenamiento más viejo al más nuevo, como los devuelve el backend. */
  readonly puntos = input.required<PuntoProgresion[]>();

  private readonly locale = inject(LOCALE_ID);
  protected readonly alto = ALTO;
  protected readonly ancho = signal(600);

  constructor() {
    const host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
    const observer = new ResizeObserver(([entrada]) =>
      this.ancho.set(Math.max(240, Math.floor(entrada.contentRect.width))),
    );
    afterNextRender(() => observer.observe(host));
    inject(DestroyRef).onDestroy(() => observer.disconnect());
  }

  protected readonly resumen = computed(() => {
    const puntos = this.puntos();
    if (puntos.length === 0) return null;

    const primero = puntos[0];
    const ultimo = puntos[puntos.length - 1];
    const mejor = puntos.reduce((max, p) => (p.pesoMaximo > max.pesoMaximo ? p : max));
    const variacion = ultimo.pesoMaximo - primero.pesoMaximo;
    const porcentaje = primero.pesoMaximo > 0 ? (variacion / primero.pesoMaximo) * 100 : null;

    return { primero, ultimo, mejor, variacion, porcentaje, cantidad: puntos.length };
  });

  /** Texto para lectores de pantalla: lo mismo que se ve en el gráfico, dicho en una frase. */
  protected readonly descripcion = computed(() => {
    const r = this.resumen();
    if (!r) return '';
    const fecha = (f: string) => formatDate(f, 'dd/MM/yyyy', this.locale);
    return (
      `Progresión en ${r.cantidad} entrenamientos: de ${r.primero.pesoMaximo} kg el ${fecha(r.primero.fecha)} ` +
      `a ${r.ultimo.pesoMaximo} kg el ${fecha(r.ultimo.fecha)}. Mejor marca: ${r.mejor.pesoMaximo} kg.`
    );
  });

  protected readonly grafico = computed(() => {
    const puntos = this.puntos();
    if (puntos.length === 0) return null;

    const x0 = MARGEN.izquierda;
    const x1 = this.ancho() - MARGEN.derecha;
    const y0 = MARGEN.arriba;
    const y1 = ALTO - MARGEN.abajo;

    const tiempos = puntos.map((p) => aTiempo(p.fecha));
    const t0 = tiempos[0];
    const t1 = tiempos[tiempos.length - 1];
    const pesos = puntos.map((p) => p.pesoMaximo);
    const pesoMaximo = Math.max(...pesos);
    const escala = escalaRedonda(pesos);

    // Eje X por fecha real: si pasaron dos semanas entre entrenamientos, se ve el hueco.
    const x = (t: number) => (t1 === t0 ? (x0 + x1) / 2 : x0 + ((t - t0) / (t1 - t0)) * (x1 - x0));
    const y = (peso: number) => y1 - ((peso - escala.min) / (escala.max - escala.min)) * (y1 - y0);

    const marcas = puntos.map((p, i) => ({
      ...p,
      cx: x(tiempos[i]),
      cy: y(p.pesoMaximo),
      esMejor: p.pesoMaximo === pesoMaximo,
    }));
    const linea = marcas.map((m, i) => `${i === 0 ? 'M' : 'L'}${m.cx},${m.cy}`).join(' ');
    const area =
      marcas.length > 1
        ? `${linea} L${marcas[marcas.length - 1].cx},${y1} L${marcas[0].cx},${y1} Z`
        : null;

    // Etiquetas de fecha repartidas en el tiempo (no por punto), así no se pisan si hay entrenamientos seguidos.
    const cantidadFechas = t1 === t0 ? 1 : Math.max(2, Math.min(5, Math.floor((x1 - x0) / 80)));
    const ejeX = Array.from({ length: cantidadFechas }, (_, i) => {
      const t = cantidadFechas === 1 ? t0 : t0 + (i * (t1 - t0)) / (cantidadFechas - 1);
      return { fecha: new Date(t), x: x(t) };
    });
    const formatoFecha = t1 - t0 > 300 * MS_POR_DIA ? 'MM/yy' : 'dd/MM';

    const ejeY = escala.marcas.map((valor) => ({ valor, y: y(valor) }));

    return { x0, x1, y1, marcas, linea, area, ejeX, ejeY, formatoFecha };
  });

  protected anclaFecha(primera: boolean, ultima: boolean): string {
    if (primera && !ultima) return 'start';
    if (ultima && !primera) return 'end';
    return 'middle';
  }
}

/** "yyyy-MM-dd" a milisegundos en hora local, para que el día no se corra por la zona horaria. */
function aTiempo(fecha: string): number {
  const [anio, mes, dia] = fecha.split('-').map(Number);
  return new Date(anio, mes - 1, dia).getTime();
}

/**
 * Rango del eje Y con valores redondos (pasos de 1, 2, 2.5 o 5 por potencia de 10)
 * que contiene a todos los pesos. Nunca baja de 0.
 */
function escalaRedonda(pesos: number[]): { min: number; max: number; marcas: number[] } {
  let min = Math.min(...pesos);
  let max = Math.max(...pesos);
  if (min === max) {
    min = Math.max(0, min - 5);
    max = max + 5;
  }

  const crudo = (max - min) / 4;
  const magnitud = 10 ** Math.floor(Math.log10(crudo));
  const paso = [1, 2, 2.5, 5, 10].map((f) => f * magnitud).find((p) => p >= crudo) ?? crudo;
  const desde = Math.floor(min / paso) * paso;
  const hasta = Math.ceil(max / paso) * paso;

  const marcas: number[] = [];
  for (let v = desde; v <= hasta + paso / 2; v += paso) {
    marcas.push(Number(v.toFixed(2)));
  }
  return { min: desde, max: hasta, marcas };
}
