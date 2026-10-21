import { Musculo } from './musculo';

export interface Ejercicio {
  id: number;
  nombre: string;
  descripcion: string;
  musculosPrincipales: Musculo[];
  musculosSecundarios: Musculo[];
}
