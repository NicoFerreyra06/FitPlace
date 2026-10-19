import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Usuario } from '../../core/usuario/usuario';

@Component({
  imports: [DecimalPipe, RouterLink],
  selector: 'app-perfil',
  styleUrl: './perfil.css',
  templateUrl: './perfil.html',
})
export class Perfil {
  private readonly usuarioService = inject(Usuario);

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getMiPerfil(),
  });
}
