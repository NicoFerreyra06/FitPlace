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
  ejercicios: EjercicioRutinaRequest[];
}

export interface RutinaRequest {
  nombre: string;
  esPublica: boolean;
  dias: DiaRutinaRequest[];
}
