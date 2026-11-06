import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { Auth } from '../../core/auth/auth';
import { Usuario } from '../../core/usuario/usuario';


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

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly inicialPerfil = computed(() => {
      return this.perfil.hasValue() ? this.perfil.value().username[0].toUpperCase() : ''
    }
  )

  protected readonly menuAbierto = signal<boolean>(false);

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
