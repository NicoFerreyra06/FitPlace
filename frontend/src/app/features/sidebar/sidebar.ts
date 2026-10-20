import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Auth } from '../../core/auth/auth';

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
