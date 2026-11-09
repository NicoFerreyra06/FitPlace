import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import {
  etiquetaRol,
  RolAsignable,
  Usuario,
  UsuarioAdmin,
} from '../../../core/usuario/usuario';

@Component({
  selector: 'app-usuarios-admin',
  templateUrl: './usuarios-admin.html',
  styleUrl: './usuarios-admin.css',
})
export class UsuariosAdmin {
  private readonly usuarioService = inject(Usuario);

  protected readonly pagina = signal(0);
  protected readonly usuarios = rxResource({
    params: () => this.pagina(),
    stream: ({ params }) => this.usuarioService.getUsuarios(params),
  });

  protected readonly usuarioSeleccionado = signal<UsuarioAdmin | null>(null);
  protected readonly detalleUsuario = signal<UsuarioAdmin | null>(null);
  protected readonly rolSeleccionado = signal<RolAsignable | ''>('');
  protected readonly cargandoDetalle = signal(false);
  protected readonly guardandoRol = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly etiquetaRol = etiquetaRol;

  protected verDetalle(usuario: UsuarioAdmin): void {
    this.usuarioSeleccionado.set(usuario);
    this.detalleUsuario.set(null);
    this.rolSeleccionado.set('');
    this.error.set(null);
    this.exito.set(null);
    this.cargandoDetalle.set(true);

    this.usuarioService.getUsuario(usuario.id).subscribe({
      next: (detalle) => {
        this.detalleUsuario.set(detalle);
        if (detalle.rol !== 'ADMIN') {
          this.rolSeleccionado.set(detalle.rol);
        }
        this.cargandoDetalle.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el detalle del usuario. Intentá de nuevo.');
        this.cargandoDetalle.set(false);
      },
    });
  }

  protected cerrarDetalle(): void {
    this.usuarioSeleccionado.set(null);
    this.detalleUsuario.set(null);
    this.error.set(null);
    this.exito.set(null);
  }

  protected seleccionarRol(event: Event): void {
    this.rolSeleccionado.set((event.target as HTMLSelectElement).value as RolAsignable);
  }

  protected guardarRol(): void {
    const usuario = this.detalleUsuario();
    const nuevoRol = this.rolSeleccionado();
    if (!usuario || !nuevoRol || usuario.rol === 'ADMIN' || this.guardandoRol()) return;

    this.guardandoRol.set(true);
    this.error.set(null);
    this.exito.set(null);

    this.usuarioService.cambiarRol(usuario.id, nuevoRol).subscribe({
      next: (actualizado) => {
        this.detalleUsuario.set(actualizado);
        this.rolSeleccionado.set(actualizado.rol === 'ADMIN' ? '' : actualizado.rol);
        this.exito.set('El rol del usuario se actualizó correctamente.');
        this.usuarios.reload();
        this.guardandoRol.set(false);
      },
      error: () => {
        this.error.set('No se pudo actualizar el rol. Verificá los permisos e intentá de nuevo.');
        this.guardandoRol.set(false);
      },
    });
  }

  protected cambiarPagina(delta: number): void {
    this.pagina.update((pagina) => pagina + delta);
  }
}
