import { Musculo } from '../musculos/musculo.model';

export interface Ejercicio {
  id: number;
  nombre: string;
  descripcion: string;
  activo?: boolean;
  musculosPrincipales: Musculo[];
  musculosSecundarios: Musculo[];
}
