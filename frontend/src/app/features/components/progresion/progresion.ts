import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  viewChild,
} from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import type ApexCharts from 'apexcharts';
import { PuntoProgresion } from '../../../core/entrenamiento/entrenamiento';
import { aSerieApex, aTiempo, formatearFecha, PuntoSerie } from './progresion.serie';

type Opciones = ApexCharts.ApexOptions;

const ALTO = 240;
const MS_POR_DIA = 86_400_000;

/**
 * Carga ApexCharts con el bundle mínimo de área más la extensión de anotaciones (la que
 * resalta la mejor marca). Va aparte del bundle principal para no encarecer la carga inicial.
 */
function cargarApex(): Promise<{ default: typeof ApexCharts }> {
  return Promise.all([
    import('apexcharts/area'),
    import('apexcharts/features/annotations'),
  ]).then(([modulo]) => modulo);
}

/**
 * Resumen y gráfico de la progresión de un ejercicio. Solo dibuja: recibe los puntos y no sabe
 * de quién son, así sirve igual para la progresión propia, la de un amigo y la de un alumno.
 *
 * El gráfico lo dibuja ApexCharts, que se carga con un import dinámico para que la librería
 * quede en un chunk aparte y no pese en la carga inicial de la app.
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

  private readonly contenedor = viewChild<ElementRef<HTMLDivElement>>('grafico');
  private readonly destroyRef = inject(DestroyRef);
  private chart: ApexCharts | null = null;
  private modulo: Promise<{ default: typeof ApexCharts }> | null = null;
  private version = 0;
  private destruido = false;
  private primerCambio = true;

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

  /** Texto para lectores de pantalla: lo mismo que muestra el gráfico, dicho en una frase. */
  protected readonly descripcion = computed(() => {
    const r = this.resumen();
    if (!r) return '';
    return (
      `Progresión en ${r.cantidad} entrenamientos: de ${r.primero.pesoMaximo} kg el ${formatearFecha(aTiempo(r.primero.fecha))} ` +
      `a ${r.ultimo.pesoMaximo} kg el ${formatearFecha(aTiempo(r.ultimo.fecha))}. Mejor marca: ${r.mejor.pesoMaximo} kg.`
    );
  });

  private readonly serie = computed(() => aSerieApex(this.puntos()));

  private readonly mejorPunto = computed<PuntoSerie | null>(() => {
    const mejor = this.resumen()?.mejor;
    return mejor ? { x: aTiempo(mejor.fecha), y: mejor.pesoMaximo } : null;
  });

  constructor() {
    afterNextRender(() => void this.dibujar());

    effect(() => {
      // Se registra la dependencia desde el primer cambio, pero el primer dibujo lo hace
      // afterNextRender, cuando el contenedor ya existe.
      this.serie();
      if (this.primerCambio) {
        this.primerCambio = false;
        return;
      }
      void this.dibujar();
    });

    this.destroyRef.onDestroy(() => {
      this.destruido = true;
      this.chart?.destroy();
      this.chart = null;
    });
  }

  /**
   * Crea el gráfico desde cero con los datos actuales. Se recrea en cada cambio porque es más
   * simple y predecible que sincronizar serie y anotaciones por separado.
   */
  private async dibujar(): Promise<void> {
    const contenedor = this.contenedor()?.nativeElement;
    if (!contenedor || this.destruido) return;

    const version = ++this.version;
    const modulo = (this.modulo ??= cargarApex());
    const { default: Apex } = await modulo;

    // Mientras cargaba la librería pudo llegar otro cambio o destruirse el componente.
    if (this.destruido || version !== this.version) return;

    this.chart?.destroy();
    const chart = new Apex(contenedor, this.opciones(this.serie(), this.mejorPunto()));
    this.chart = chart;
    await chart.render();
  }

  private opciones(serie: PuntoSerie[], mejor: PuntoSerie | null): Opciones {
    const dias = serie.length > 1 ? (serie[serie.length - 1].x - serie[0].x) / MS_POR_DIA : 0;

    return {
      chart: {
        type: 'area',
        height: ALTO,
        background: 'transparent',
        fontFamily: 'inherit',
        foreColor: '#a1a1a6',
        toolbar: { show: false },
        zoom: { enabled: false },
        animations: { enabled: true, speed: 400 },
      },
      series: [{ name: 'Peso máximo', data: serie }],
      colors: ['#c8f169'],
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: 2 },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 0, opacityFrom: 0.3, opacityTo: 0, stops: [0, 100] },
      },
      markers: {
        size: 3,
        colors: ['#0b0b0c'],
        strokeColors: '#c8f169',
        strokeWidth: 2,
        hover: { size: 5 },
      },
      grid: {
        borderColor: 'rgba(255, 255, 255, 0.1)',
        strokeDashArray: 4,
        padding: { left: 8, right: 12 },
      },
      xaxis: {
        type: 'datetime',
        labels: {
          datetimeUTC: false,
          format: dias > 300 ? 'MM/yy' : 'dd/MM',
          style: { colors: '#a1a1a6', fontSize: '11px' },
        },
        axisBorder: { show: false },
        axisTicks: { show: false },
        tooltip: { enabled: false },
      },
      yaxis: {
        labels: {
          formatter: (valor: number) => `${valor}`,
          style: { colors: '#a1a1a6', fontSize: '11px' },
        },
      },
      tooltip: {
        theme: 'dark',
        x: { formatter: (valor: number) => formatearFecha(valor) },
        y: { formatter: (valor: number) => `${valor} kg` },
      },
      annotations: mejor
        ? {
            points: [
              {
                x: mejor.x,
                y: mejor.y,
                marker: {
                  size: 6,
                  fillColor: '#c8f169',
                  strokeColor: '#0b0b0c',
                  strokeWidth: 2,
                },
                label: {
                  text: `Mejor: ${mejor.y} kg`,
                  borderColor: '#c8f169',
                  offsetY: -6,
                  style: { background: '#18181a', color: '#c8f169', fontSize: '11px' },
                },
              },
            ],
          }
        : undefined,
      legend: { show: false },
      noData: {
        text: 'Sin datos para graficar',
        style: { color: '#a1a1a6', fontSize: '13px' },
      },
    };
  }
}
