import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin } from 'rxjs';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';
import {
  DiaRutinaResponse,
  DIAS_SEMANA,
  etiquetaDia,
  RutinaResponse,
} from '../../../core/rutinas/rutinas.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  imports: [RouterLink, FormsModule],
  selector: 'app-rutinas-publicas',
  styleUrl: './rutinas-publicas.css',
  templateUrl: './rutinas-publicas.html',
})
export class RutinasPublicas implements OnInit {
  private readonly rutinasService = inject(Rutinas);
  private readonly usuarioService = inject(Usuario);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly rutinas = signal<RutinaResponse[]>([]);
  protected readonly rutinaActivaId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);
  protected readonly busqueda = signal<string>('');
  protected readonly activandoId = signal<number | null>(null);

  protected readonly rutinasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const lista = this.rutinas();
    if (!texto) {
      return lista;
    }
    return lista.filter((r) => r.nombre.toLowerCase().includes(texto));
  });

  etiquetaDia = etiquetaDia;

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.cargando.set(true);
    forkJoin({
      rutinas: this.rutinasService.getRutinasPublicas(),
      usuario: this.usuarioService.getMiPerfil(),
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: ({ rutinas, usuario }) => {
        this.rutinas.set(rutinas.content);
        this.rutinaActivaId.set(usuario.rutinaActivaId);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(
          err.error?.message ?? 'No se pudieron cargar las rutinas públicas',
        );
        this.cargando.set(false);
      },
    });
  }

  ordenarDias(dias: DiaRutinaResponse[]): DiaRutinaResponse[] {
    const orden = DIAS_SEMANA.map((d) => d.valor);
    return [...dias].sort(
      (a, b) => orden.indexOf(a.diaDeLaSemana) - orden.indexOf(b.diaDeLaSemana),
    );
  }

  usar(rutina: RutinaResponse) {
    this.activandoId.set(rutina.id);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.usuarioService.activarRutina(rutina.id).subscribe({
      next: (usuario) => {
        this.rutinaActivaId.set(usuario.rutinaActivaId);
        this.activandoId.set(null);
        this.mensajeExito.set(`¡Ahora estás usando la rutina "${rutina.nombre}"!`);
      },
      error: (err) => {
        this.activandoId.set(null);
        this.errorMsg.set(
          err.error?.message ?? 'No se pudo activar la rutina seleccionada',
        );
      },
    });
  }

  desactivar(rutina: RutinaResponse) {
    this.activandoId.set(rutina.id);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.usuarioService.desactivarRutina().subscribe({
      next: () => {
        this.rutinaActivaId.set(null);
        this.activandoId.set(null);
      },
      error: (err) => {
        this.activandoId.set(null);
        this.errorMsg.set(err.error?.message ?? 'No se pudo desactivar la rutina');
      },
    });
  }
}
