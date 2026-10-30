import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { DiaRutinaResponse, DIAS_SEMANA, etiquetaDia, RutinaResponse } from '../../../core/rutinas/rutinas.model';
import { Ejercicio } from '../../../model/ejercicio';
import { EjercicioDetails } from '../../components/ejercicios/ejercicio-details/ejercicio-details';
import { EjercicioService } from '../../../core/services/ejercicio-service';
import { Usuario } from '../../../core/usuario/usuario';
import { rxResource } from '@angular/core/rxjs-interop';

@Component({
  imports: [RouterLink, EjercicioDetails],
  selector: 'app-rutina-details',
  styleUrl: './rutina-details.css',
  templateUrl: './rutina-details.html',
})
export class RutinaDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly rutinasService = inject(Rutinas);
  private readonly ejercicioService = inject(EjercicioService);
  private readonly usuarioService = inject(Usuario);

  protected readonly cargando = signal(true);
  protected readonly rutina = signal<RutinaResponse | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly diasAbiertos = signal<Set<number>>(new Set());
  protected readonly ejercicioSeleccionado = signal<Ejercicio | null>(null);

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly esDuenio = computed(() => {
    const r = this.rutina();

    if(!this.perfil.hasValue()){
      return false;
    }
    const p = this.perfil.value();
    return Boolean(r && r.creadorId === p.id);
  })

  protected readonly diasOrdenados = computed( () => {
    const r = this.rutina();
    if(!r || !r.diaRutinas) return;

    const orden = DIAS_SEMANA.map((dia) => dia.valor);
    return [...r.diaRutinas].sort((a,b) => orden.indexOf(a.diaDeLaSemana) - orden.indexOf(b.diaDeLaSemana));
  });

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

  verDetalle(idEjercicio: number): void {
    this.ejercicioService.getEjercicioById(idEjercicio).subscribe({
      next: (ej) => this.ejercicioSeleccionado.set(ej),
      error: (err) => console.error('Error al obtener el detalle del ejercicio.', err)
    });
  }

  cerrarDetalle(): void {
    this.ejercicioSeleccionado.set(null);
  }
}
