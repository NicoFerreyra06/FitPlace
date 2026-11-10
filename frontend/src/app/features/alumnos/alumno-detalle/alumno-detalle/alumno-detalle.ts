import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AlumnoResponse, AlumnoService } from '../../../../core/alumnos/alumno';
import { Entrenamiento, EntrenamientoLog } from '../../../../core/entrenamiento/entrenamiento';
import { Rutinas } from '../../../../core/rutinas/rutinas';
import { etiquetaRol } from '../../../../core/usuario/usuario';
import { Progresion } from '../../../components/progresion/progresion';
import { Rachas } from '../../../components/rachas/rachas';
import { DiaRutinaResponse, DIAS_SEMANA, etiquetaDia, RutinaResponse } from '../../../../core/rutinas/rutinas.model';
import { catchError, forkJoin, of } from 'rxjs';

interface RecordDerivado {
  nombreEjercicio: string;
  pesoMaximo: number;
  fechaLogro: string;
}

@Component({
  imports: [DatePipe, DecimalPipe, RouterLink, Rachas, Progresion],
  selector: 'app-alumno-detalle',
  styleUrl: './alumno-detalle.css',
  templateUrl: './alumno-detalle.html',
})
export class AlumnoDetalle {
  private readonly route = inject(ActivatedRoute);
  private readonly alumnoService = inject(AlumnoService);
  private readonly entrenamientoService = inject(Entrenamiento);
  private readonly rutinasService = inject(Rutinas);
  private readonly destroyRef = inject(DestroyRef);

  private readonly alumnoId = this.route.snapshot.paramMap.get('id') ?? '';
  private readonly idValido = this.alumnoId.length > 0;

  protected readonly modalAbierto = signal<boolean>(false);
  protected readonly rutinasDisponibles = signal<RutinaResponse[]>([]);
  protected readonly cargandoRutinas = signal<boolean>(false);
  protected readonly errorAsignacion = signal<string | null>(null);
  protected readonly asignando = signal<number | null>(null);
  protected readonly misRutinaIds = signal<Set<number>>(new Set());
  protected readonly rutinaExpandidaId = signal<number | null>(null);

  protected readonly resultado = rxResource({
    params: () => (this.idValido ? { id: this.alumnoId } : undefined),
    stream: ({ params }) => this.alumnoService.getAlumnoById(params.id),
  });

  protected readonly alumno = computed<AlumnoResponse | null>(() =>
    this.resultado.hasValue() ? this.resultado.value() : null,
  );

  protected readonly noEncontrado = computed(
    () =>
      !this.idValido ||
      (this.resultado.hasValue() && this.resultado.value() === null),
  );

  protected readonly historial = rxResource({
    params: () => {
      const alumno = this.alumno();
      return alumno ? { id: alumno.id } : undefined;
    },
    stream: ({ params }) =>
      this.entrenamientoService.getHistorialAlumnoCompleto(params.id),
  });

  protected readonly logs = computed<EntrenamientoLog[]>(() =>
    this.historial.hasValue() ? this.historial.value() : [],
  );

  protected readonly records = computed<RecordDerivado[]>(() => {
    const porEjercicio = new Map<string, RecordDerivado>();

    for (const log of this.logs()) {
      for (const marca of log.marcasEjercicio) {
        const actual = porEjercicio.get(marca.nombreEjercicio);

        if (!actual || marca.pesoLevantado > actual.pesoMaximo) {
          porEjercicio.set(marca.nombreEjercicio, {
            nombreEjercicio: marca.nombreEjercicio,
            pesoMaximo: marca.pesoLevantado,
            fechaLogro: log.fecha,
          });
        }
      }
    }

    return [...porEjercicio.values()].sort((a, b) => b.fechaLogro.localeCompare(a.fechaLogro));
  });

  protected readonly mostrarTodosLosRecords = signal(false);

  protected readonly recordsVisibles = computed(() =>
    this.mostrarTodosLosRecords() ? this.records() : this.records().slice(0, 3),
  );

  protected readonly hayMasRecords = computed(() => this.records().length > 3);

  protected alternarRecords(): void {
    this.mostrarTodosLosRecords.update((mostrando) => !mostrando);
  }

  protected readonly rutina = rxResource({
    params: () => {
      const alumno = this.alumno();
      return alumno?.rutinaActivaId ? { id: alumno.rutinaActivaId } : undefined;
    },
    stream: ({ params }) => this.rutinasService.getRutina(params.id),
  });

  protected readonly rutinaPrivada = computed(() => {
    const error = this.rutina.error();
    return error instanceof HttpErrorResponse && error.status === 403;
  });

  protected readonly rutinaEsPrivada = computed(() => {
    const rutina = this.rutina.hasValue() ? this.rutina.value() : null;
    return rutina ? !rutina.esPublica : false;
  });

  protected readonly nombreRutina = computed(() =>
    this.rutina.hasValue() ? (this.rutina.value()?.nombre ?? null) : null,
  );

  protected readonly rutinaAbierta = signal(false);

  protected readonly diasRutina = computed<DiaRutinaResponse[]>(() => {
    const rutina = this.rutina.hasValue() ? this.rutina.value() : null;
    const dias = rutina?.diaRutinas ?? [];

    const orden = DIAS_SEMANA.map((dia) => dia.valor);
    return [...dias].sort(
      (a, b) =>
        orden.indexOf(a.diaDeLaSemana) - orden.indexOf(b.diaDeLaSemana),
    );
  });

  protected alternarRutina(): void {
    this.rutinaAbierta.update((abierta) => !abierta);
  }

  protected readonly etiquetaDia = etiquetaDia;

  protected readonly ejercicioAbierto = signal<string | null>(null);

  protected readonly puntosProgresion = computed(() => {
    const nombre = this.ejercicioAbierto();
    if (!nombre) return [];

    const porFecha = new Map<string, number>();

    for (const log of this.logs()) {
      for (const marca of log.marcasEjercicio) {
        if (marca.nombreEjercicio !== nombre) continue;

        const actual = porFecha.get(log.fecha);
        if (actual === undefined || marca.pesoLevantado > actual) {
          porFecha.set(log.fecha, marca.pesoLevantado);
        }
      }
    }

    return [...porFecha.entries()].map(([fecha, pesoMaximo]) => ({ fecha, pesoMaximo }))
      .sort((a, b) => a.fecha.localeCompare(b.fecha));
  });

  protected alternarProgresion(nombreEjercicio: string): void {
    this.ejercicioAbierto.update((abierto) => abierto === nombreEjercicio ? null : nombreEjercicio);
  }

  protected readonly etiquetaRol = etiquetaRol;

  protected abrirModal(): void {
    this.modalAbierto.set(true);
    this.errorAsignacion.set(null);
    
    if (this.rutinasDisponibles().length > 0) return;
    this.cargandoRutinas.set(true);
    forkJoin({
      propias: this.rutinasService.getMisRutinas(),
      publicas: this.rutinasService.getRutinasPublicas(0, 100)
    }).pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((resultado) => {
        this.cargandoRutinas.set(false);
        if (!resultado) return;
        const { propias, publicas } = resultado;
        const propiaIds = new Set(propias.content.map((r) => r.id));
        this.misRutinaIds.set(propiaIds);
        this.rutinasDisponibles.set([...propias.content, ...publicas.content.filter((r) => !propiaIds.has(r.id))]);
      });
  }

  protected cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  protected asignarRutina(idRutina: number): void {
    const alumno = this.alumno();
    if (!alumno) return;
    this.asignando.set(idRutina);
    this.errorAsignacion.set(null);
    this.alumnoService
      .asignarRutina(alumno.id, idRutina)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.resultado.reload();
          this.asignando.set(null);
          this.cerrarModal();
        },
        error: (err) => {
          this.errorAsignacion.set(
            err.error?.message ?? 'No se pudo asignar la rutina',
          );
          this.asignando.set(null);
        },
      });
  }

  protected alternarDetalleRutina(id: number): void {
    this.rutinaExpandidaId.update((abierto) => (abierto === id ? null : id));
  }

  protected esMia(rutina: RutinaResponse): boolean {
    return this.misRutinaIds().has(rutina.id);
  }
}