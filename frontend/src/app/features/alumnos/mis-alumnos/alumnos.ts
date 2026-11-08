import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { AlumnoResponse, AlumnoService } from '../../../core/alumnos/alumno';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

@Component({
  imports: [RouterLink],
  selector: 'app-alumnos',
  styleUrl: './alumnos.css',
  templateUrl: './alumnos.html',
})
export class Alumnos implements OnInit{
  private readonly alumnoService = inject(AlumnoService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly alumnos = signal<AlumnoResponse[]>([]);
  protected readonly confirmandoId = signal<number | null>(null);
  protected readonly eliminandoId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);

  protected readonly totalAlumnos = computed(() => this.alumnos().length);

  ngOnInit() {
    this.cargarAlumnos();
  }

  private cargarAlumnos() {
    this.cargando.set(true);
    this.errorMsg.set(null);

    this.alumnoService
      .getAlumnos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (alumnos) => {
          this.alumnos.set(
            [...alumnos].sort((a, b) => a.username.localeCompare(b.username, 'es')),
          );
          this.cargando.set(false);
        },
        error: (err) => {
          this.errorMsg.set(err.error?.message ?? 'No se pudo cargar tu lista de alumnos');
          this.cargando.set(false);
        },
      });
  }

  protected pedirBaja(alumno: AlumnoResponse) {
    this.errorMsg.set(null);
    this.mensajeExito.set(null);
    this.confirmandoId.set(alumno.id);
  }

  protected cancelarBaja() {
    this.confirmandoId.set(null);
  }

  protected darDeBaja(alumno: AlumnoResponse) {
    if (this.eliminandoId() !== null) return;

    this.eliminandoId.set(alumno.id);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.alumnoService
      .eliminarAlumno(alumno.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.alumnos.update((lista) => lista.filter((a) => a.id !== alumno.id));
          this.eliminandoId.set(null);
          this.confirmandoId.set(null);
          this.mensajeExito.set(`Diste de baja a ${alumno.username}.`);
        },
        error: (err) => {
          this.eliminandoId.set(null);
          this.errorMsg.set(err.error?.message ?? 'No se pudo dar de baja al alumno');
        },
      });
  }
}
