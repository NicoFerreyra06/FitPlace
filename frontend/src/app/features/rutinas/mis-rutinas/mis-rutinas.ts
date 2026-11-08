import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';
import { RutinaResponse } from '../../../core/rutinas/rutinas.model';
import { Entrenamiento } from '../../../core/entrenamiento/entrenamiento';
import { EntrenadorService } from '../../../core/entrenador/entrenador';

@Component({
  imports: [RouterLink],
  selector: 'app-mis-rutinas',
  styleUrl: './mis-rutinas.css',
  templateUrl: './mis-rutinas.html',
})
export class MisRutinas implements OnInit {
  private readonly rutinasService = inject(Rutinas);
  private readonly usuarioService = inject(Usuario);
  private readonly entrenamientoService = inject(Entrenamiento);
  private readonly entrenadorService = inject(EntrenadorService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly rutinas = signal<RutinaResponse[]>([]);
  protected readonly rutinaActivaId = signal<number | null>(null);
  protected readonly miId = signal<number | null>(null);
  protected readonly entrenadorId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);

  ngOnInit() {
    this.cargar();
  }

  protected esMia(rutina: RutinaResponse): boolean {
    return rutina.creadorId === this.miId();
  }

  protected esDelEntrenador(rutina: RutinaResponse): boolean {
    const entrenadorId = this.entrenadorId();
    return entrenadorId !== null && rutina.creadorId === entrenadorId;
  }

  private cargar(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    forkJoin({
      rutinas: this.rutinasService.getMisRutinas(),
      usuario: this.usuarioService.getPerfilGuardado(),
      entrenador: this.entrenadorService
        .getMiEntrenador()
        .pipe(catchError(() => of(null))),
      publicas: this.rutinasService
        .getRutinasPublicas(0, 100)
        .pipe(catchError(() => of(null))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ rutinas, usuario, entrenador, publicas }) => {
          this.miId.set(usuario.id);
          this.entrenadorId.set(
            entrenador ? Number(entrenador.id) : null,
          );
          this.rutinaActivaId.set(usuario.rutinaActivaId);
          this.rutinas.set(rutinas.content);

          const activaEsPropia =
            usuario.rutinaActivaId !== null &&
            rutinas.content.some((r) => r.id === usuario.rutinaActivaId);

          if (!entrenador && activaEsPropia) {
            this.cargando.set(false);
            return;
          }

          this.cargarRutinasDelEntrenador(
            rutinas.content,
            publicas?.content ?? [],
            usuario.rutinaActivaId,
            usuario.id,
            entrenador ? Number(entrenador.id) : null,
          );
        },
        error: (err) => {
          this.errorMsg.set(
            err.error?.message ?? 'No se pudo conectar con el servidor',
          );
          this.cargando.set(false);
        },
      });
  }

  private cargarRutinasDelEntrenador(propias: RutinaResponse[], publicas: RutinaResponse[], activaId: number | null, miId: number, entrenadorId: number | null): void {
    const publicasDelEntrenador =
      entrenadorId === null
        ? []
        : publicas.filter(
            (rutina) =>
              rutina.creadorId === entrenadorId &&
              !propias.some((propia) => propia.id === rutina.id),
          );

    this.entrenamientoService
      .getHistorialCompleto(miId)
      .pipe(
        catchError(() => of([])),
        map((logs) => {
          const ids = new Set<number>();

          for (const log of logs) {
            ids.add(log.idRutina);
          }

          if (activaId) ids.add(activaId);

          return [...ids].filter(
            (id) =>
              !propias.some((rutina) => rutina.id === id) &&
              !publicasDelEntrenador.some((rutina) => rutina.id === id),
          );
        }),
        switchMap((ids) => {
          if (ids.length === 0) return of([]);

          return forkJoin(
            ids.map((id) =>
              this.rutinasService.getRutina(id).pipe(
                catchError((error: HttpErrorResponse) => {
                  // Si la rutina activa ya no es accesible (por ejemplo,
                  // cambié de entrenador), deja de estar activa.
                  if (
                    id === activaId &&
                    (error.status === 403 || error.status === 404)
                  ) {
                    this.desactivarRutinaInaccesible();
                  }
                  return of(null);
                }),
              ),
            ),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (encontradas) => {
          const delEntrenador = encontradas.filter(
            (rutina): rutina is RutinaResponse =>
              rutina !== null &&
              (rutina.creadorId === entrenadorId || rutina.id === activaId),
          );

          this.rutinas.set([
            ...publicasDelEntrenador,
            ...delEntrenador,
            ...propias,
          ]);
          this.cargando.set(false);
        },
        error: () => {
          this.cargando.set(false);
        },
      });
  }

  private desactivarRutinaInaccesible(): void {
    this.usuarioService
      .desactivarRutina()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.rutinaActivaId.set(null),
        error: () => this.rutinaActivaId.set(null),
      });
  }

  activar(rutina: RutinaResponse) {
    this.errorMsg.set(null);
    this.usuarioService.activarRutina(rutina.id).subscribe({
      next: (usuario) => this.rutinaActivaId.set(usuario.rutinaActivaId),
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo activar la rutina'),
    });
  }

  desactivar() {
    this.errorMsg.set(null);
    this.usuarioService.desactivarRutina().subscribe({
      next: () => this.rutinaActivaId.set(null),
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo desactivar la rutina'),
    });
  }

  eliminar(rutina: RutinaResponse) {
    if (!confirm(`¿Seguro que querés eliminar la rutina "${rutina.nombre}"?`)) {
      return;
    }

    this.errorMsg.set(null);
    this.rutinasService.eliminar(rutina.id).subscribe({
      next: () => {
        this.rutinas.update((lista) => lista.filter((r) => r.id !== rutina.id));
        if (this.rutinaActivaId() === rutina.id) {
          this.rutinaActivaId.set(null);
        }
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo eliminar la rutina'),
    });
  }
}
