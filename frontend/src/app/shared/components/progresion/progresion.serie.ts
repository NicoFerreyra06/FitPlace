import { PuntoProgresion } from '../../../core/entrenamiento/entrenamiento';

/** Punto en el formato que espera ApexCharts: x en milisegundos, y en kg. */
export interface PuntoSerie {
  x: number;
  y: number;
}

/**
 * "yyyy-MM-dd" a milisegundos en hora local.
 *
 * Se arma con `new Date(anio, mes - 1, dia)` a propósito: `new Date("2026-10-01")`
 * se interpreta como UTC y en Argentina mostraría el 30/09.
 */
export function aTiempo(fecha: string): number {
  const [anio, mes, dia] = fecha.split('-').map(Number);
  return new Date(anio, mes - 1, dia).getTime();
}

/** Convierte los puntos del backend a la serie que consume ApexCharts. */
export function aSerieApex(puntos: PuntoProgresion[]): PuntoSerie[] {
  return puntos.map((punto) => ({ x: aTiempo(punto.fecha), y: punto.pesoMaximo }));
}

/** Fecha legible (dd/MM/yyyy) a partir del valor del eje x, sin corrimiento por zona horaria. */
export function formatearFecha(valor: number | string | Date): string {
  const fecha = valor instanceof Date ? valor : new Date(Number(valor));
  if (Number.isNaN(fecha.getTime())) return '';

  const dia = String(fecha.getDate()).padStart(2, '0');
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  return `${dia}/${mes}/${fecha.getFullYear()}`;
}
