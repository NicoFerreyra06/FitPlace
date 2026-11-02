import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EntrenadorService, EntrenadorResponse } from '../../core/entrenador/entrenador';

@Component({
  imports: [],
  selector: 'app-entrenador',
  styleUrl: './entrenador.css',
  templateUrl: './entrenador.html',
})
export class Entrenador implements OnInit {
  private readonly entrenadorService = inject(EntrenadorService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly miEntrenador = signal<EntrenadorResponse | null>(null);
  protected readonly entrenadores = signal<EntrenadorResponse[]>([]);
  protected readonly asignando = signal<string | null>(null);
  protected readonly desvinculando = signal(false);
  protected readonly confirmandoDesvinculacion = signal(false);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);

  ngOnInit() {
    this.cargarDatos();
  }

  private cargarDatos() {
    this.cargando.set(true);
    this.errorMsg.set(null);

    forkJoin({
      entrenador: this.entrenadorService
        .getMiEntrenador()
        .pipe(catchError(() => of(null))),
      entrenadores: this.entrenadorService
        .getEntrenadores()
        .pipe(catchError(() => of([]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ entrenador, entrenadores }) => {
          this.miEntrenador.set(entrenador);
          this.entrenadores.set(entrenadores);
          this.cargando.set(false);
        },
        error: (err) => {
          this.errorMsg.set(err.error?.message ?? 'No se pudo cargar la información.');
          this.cargando.set(false);
        },
      });
  }

  protected asignar(entrenador: EntrenadorResponse) {
    if (this.asignando() !== null || this.miEntrenador()?.id === entrenador.id) return;

    this.asignando.set(entrenador.id);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.entrenadorService
      .asignarEntrenador(entrenador.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.miEntrenador.set(entrenador);
          this.asignando.set(null);
          this.mensajeExito.set(`¡${entrenador.username} es ahora tu entrenador!`);
        },
        error: (err) => {
          this.asignando.set(null);
          this.errorMsg.set(err.error?.message ?? 'No se pudo asignar el entrenador.');
        },
      });
  }

  protected pedirDesvinculacion() {
    this.errorMsg.set(null);
    this.mensajeExito.set(null);
    this.confirmandoDesvinculacion.set(true);
  }

  protected cancelarDesvinculacion() {
    this.confirmandoDesvinculacion.set(false);
  }

  protected desvincular() {
    const entrenador = this.miEntrenador();
    if (!entrenador || this.desvinculando()) return;

    this.desvinculando.set(true);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.entrenadorService
      .desvincularEntrenador()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const nombre = entrenador.username;
          this.miEntrenador.set(null);
          this.desvinculando.set(false);
          this.confirmandoDesvinculacion.set(false);
          this.mensajeExito.set(`Desvinculaste a ${nombre} como tu entrenador.`);
        },
        error: (err) => {
          this.desvinculando.set(false);
          this.errorMsg.set(err.error?.message ?? 'No se pudo desvincular el entrenador.');
        },
      });
  }
}
