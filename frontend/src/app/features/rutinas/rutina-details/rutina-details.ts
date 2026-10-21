import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Rutinas } from '../../../core/rutinas/rutinas';
import {
  DiaRutinaResponse,
  etiquetaDia,
  RutinaResponse,
} from '../../../core/rutinas/rutinas.model';

@Component({
  imports: [RouterLink],
  selector: 'app-rutina-details',
  styleUrl: './rutina-details.css',
  templateUrl: './rutina-details.html',
})
export class RutinaDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly rutinasService = inject(Rutinas);

  protected readonly cargando = signal(true);
  protected readonly rutina = signal<RutinaResponse | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly diasAbiertos = signal<Set<number>>(new Set());

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.rutinasService.getRutina(id).subscribe({
      next: (rutina) => {
        this.rutina.set(rutina);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(
          err.error?.message ?? 'No se pudo cargar la rutina',
        );
        this.cargando.set(false);
      },
    });
  }

  toggleDia(dia: DiaRutinaResponse) {
    this.diasAbiertos.update((set) => {
      const copia = new Set(set);
      if (copia.has(dia.id)) {
        copia.delete(dia.id);
      } else {
        copia.add(dia.id);
      }
      return copia;
    });
  }

  estaAbierto(dia: DiaRutinaResponse): boolean {
    return this.diasAbiertos().has(dia.id);
  }

  etiquetaDia = etiquetaDia;
}
