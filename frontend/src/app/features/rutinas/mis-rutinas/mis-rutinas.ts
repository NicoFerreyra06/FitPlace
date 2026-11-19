import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';
import { RutinaResponse } from '../../../core/rutinas/rutinas.model';
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
  private readonly entrenadorService = inject(EntrenadorService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly rutinas = signal<RutinaResponse[]>([]);
  protected readonly rutinaActivaId = signal<number | null>(null);
  protected readonly miId = signal<number | null>(null);
  protected readonly entrenadorId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly confirmandoId = signal<number | null>(null);

  protected readonly pagina = signal(0);
  protected readonly cargandoMas = signal(false);
  protected readonly hayMas = signal(false);
  private readonly tamanioPagina = 10;

  ngOnInit() {
    this.cargar();
  }

  protected esMia(rutina: RutinaResponse): boolean {
    return rutina.creadorId === this.miId();
  }

  protected esDelEntrenador(rutina: RutinaResponse): boolean {
    const entrenadorId = this.entrenadorId();
    return rutina.creadorId === entrenadorId;
  }

  private cargar(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    forkJoin({
      rutinas: this.rutinasService.getRutinasDisponibles(0, this.tamanioPagina),
      usuario: this.usuarioService.getPerfilGuardado(),
      entrenador: this.entrenadorService
        .getMiEntrenador()
        .pipe(catchError(() => of(null))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ rutinas, usuario, entrenador }) => {
          this.miId.set(usuario.id);
          this.entrenadorId.set(entrenador ? Number(entrenador.id) : null);
          this.rutinaActivaId.set(usuario.rutinaActivaId);
          this.pagina.set(0);
          this.hayMas.set(!rutinas.last);
          this.rutinas.set(rutinas.content);
          this.cargando.set(false);
        },
        error: (err) => {
          this.errorMsg.set(
            err.error?.message ?? 'No se pudo conectar con el servidor',
          );
          this.cargando.set(false);
        },
      });
  }

  protected cargarMas(): void {
    if (this.cargando() || this.cargandoMas() || !this.hayMas()) {
      return;
    }

    const siguientePagina = this.pagina() + 1;
    this.cargandoMas.set(true);
    this.errorMsg.set(null);

    this.rutinasService
      .getRutinasDisponibles(siguientePagina, this.tamanioPagina)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          const existentes = new Set(this.rutinas().map((r) => r.id));
          const nuevas = page.content.filter((r) => !existentes.has(r.id));
          this.rutinas.update((lista) => [...lista, ...nuevas]);
          this.pagina.set(siguientePagina);
          this.hayMas.set(!page.last);
          this.cargandoMas.set(false);
        },
        error: (err) => {
          this.errorMsg.set(
            err.error?.message ?? 'No se pudieron cargar más rutinas',
          );
          this.cargandoMas.set(false);
        },
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
    this.errorMsg.set(null);
    this.rutinasService.eliminar(rutina.id).subscribe({
      next: () => {
        this.confirmandoId.set(null);
        this.rutinas.update((lista) => lista.filter((r) => r.id !== rutina.id));
        if (this.rutinaActivaId() === rutina.id) {
          this.rutinaActivaId.set(null);
        }
      },
      error: (err) => {
        this.confirmandoId.set(null);
        this.errorMsg.set(err.error?.message ?? 'No se pudo eliminar la rutina');
      },
    });
  }
}
