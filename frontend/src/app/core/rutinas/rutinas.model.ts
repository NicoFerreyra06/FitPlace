export type DiaSemana =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export const DIAS_SEMANA: { valor: DiaSemana; etiqueta: string }[] = [
  { valor: 'MONDAY', etiqueta: 'Lunes' },
  { valor: 'TUESDAY', etiqueta: 'Martes' },
  { valor: 'WEDNESDAY', etiqueta: 'Miércoles' },
  { valor: 'THURSDAY', etiqueta: 'Jueves' },
  { valor: 'FRIDAY', etiqueta: 'Viernes' },
  { valor: 'SATURDAY', etiqueta: 'Sábado' },
  { valor: 'SUNDAY', etiqueta: 'Domingo' },
];

export function etiquetaDia(dia: DiaSemana): string {
  return DIAS_SEMANA.find((d) => d.valor === dia)?.etiqueta ?? dia;
}

/** Largo máximo del nombre de un día (el backend valida lo mismo). */
export const NOMBRE_DIA_MAX = 40;

// ===== Respuestas del backend =====

export interface EjercicioRutinaResponse {
  id: number;
  ejercicioId: number;
  nombreEjercicio: string;
  series: number;
  repeticiones: number;
}

export interface DiaRutinaResponse {
  id: number;
  nombreRutina: string,
  diaDeLaSemana: DiaSemana;
  /** Nombre del día ("Push A"). Es obligatorio, pero llega null en días creados antes de que existiera. */
  nombre: string | null;
  ejercicioRutinas: EjercicioRutinaResponse[];
}

export interface RutinaResponse {
  id: number;
  creadorId: number;
  nombre: string;
  tokenCompartir: string;
  esPublica: boolean;
  diaRutinas: DiaRutinaResponse[];
}

// ===== Requests al backend =====

export interface EjercicioRutinaRequest {
  ejercicioId: number;
  series: number;
  repeticiones: number;
}

export interface DiaRutinaRequest {
  diaDeLaSemana: DiaSemana;
  nombre: string;
  ejercicios: EjercicioRutinaRequest[];
}

export interface RutinaRequest {
  nombre: string;
  esPublica: boolean;
  dias: DiaRutinaRequest[];
}
