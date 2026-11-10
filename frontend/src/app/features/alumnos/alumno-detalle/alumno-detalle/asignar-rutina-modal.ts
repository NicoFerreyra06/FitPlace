import { Component, input, output } from '@angular/core';
import { RutinaResponse, etiquetaDia } from '../../../../core/rutinas/rutinas.model';

@Component({
  selector: 'app-asignar-rutina-modal',
  templateUrl: './asignar-rutina-modal.html',
  styleUrl: './asignar-rutina-modal.css',
})
export class AsignarRutinaModal {
  readonly rutinas = input.required<RutinaResponse[]>();
  readonly alumnoRutinaActivaId = input<number | null>(null);
  readonly error = input<string | null>(null);
  readonly cargando = input(false);
  readonly asignando = input<number | null>(null);
  readonly misRutinaIds = input<ReadonlySet<number>>(new Set());
  readonly rutinaExpandidaId = input<number | null>(null);

  readonly cerrar = output<void>();
  readonly asignar = output<number>();
  readonly alternarDetalle = output<number>();

  protected readonly etiquetaDia = etiquetaDia;

  protected esMia(rutina: RutinaResponse): boolean {
    return this.misRutinaIds().has(rutina.id);
  }
}
