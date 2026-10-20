import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuarios } from '../../../core/usuarios/usuarios';
import { RutinaResponse } from '../../../core/rutinas/rutinas.model';

@Component({
  imports: [RouterLink],
  selector: 'app-mis-rutinas',
  styleUrl: './mis-rutinas.css',
  templateUrl: './mis-rutinas.html',
})
export class MisRutinas implements OnInit {
  private readonly rutinasService = inject(Rutinas);
  private readonly usuariosService = inject(Usuarios);

  protected readonly cargando = signal(true);
  protected readonly rutinas = signal<RutinaResponse[]>([]);
  protected readonly rutinaActivaId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);

  ngOnInit() {
    forkJoin({
      rutinas: this.rutinasService.getMisRutinas(),
      usuario: this.usuariosService.getMe(),
    }).subscribe({
      next: ({ rutinas, usuario }) => {
        this.rutinas.set(rutinas.content);
        this.rutinaActivaId.set(usuario.rutinaActivaId);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor');
        this.cargando.set(false);
      },
    });
  }

  activar(rutina: RutinaResponse) {
    this.errorMsg.set(null);
    this.usuariosService.activarRutina(rutina.id).subscribe({
      next: (usuario) => this.rutinaActivaId.set(usuario.rutinaActivaId),
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo activar la rutina'),
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
