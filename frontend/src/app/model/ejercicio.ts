import { Musculo } from "./musculo";

export interface Ejercicio {
    id: string | number,
    nombre: string,
    descripcion: string,
    musculosPrincipales: Musculo[],
    musculosSecundarios: Musculo[]

}