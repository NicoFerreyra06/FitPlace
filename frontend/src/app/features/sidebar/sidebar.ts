import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import { Usuario } from '../../core/usuario/usuario';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-sidebar',
  styleUrl: './sidebar.css',
  templateUrl: './sidebar.html',
  host: { '(document:keydown.escape)': 'cerrarMenu()' },
})
export class Sidebar {
  private readonly router = inject(Router);
  private readonly auth = inject(Auth);
  private readonly usuarioService = inject(Usuario);

  protected readonly menuAbierto = signal<boolean>(false);

  protected readonly perfilActivo = toSignal(
    this.usuarioService.getPerfilGuardado().pipe(catchError(() => of(null))),
    { initialValue: null }
  );

  protected readonly esEntrenador = computed(() => {
    return this.perfilActivo()?.rol === 'ENTRENADOR'
  });

  protected readonly esAdmin = computed(() => {
    return this.perfilActivo()?.rol === 'ADMIN'
  });

  abrirMenu() {
    this.menuAbierto.set(true);
  }

  cerrarMenu() {
    this.menuAbierto.set(false);
  }

  cerrarSesion() {
    this.auth.logOut();
    this.router.navigate(['/login']);
  }
}
