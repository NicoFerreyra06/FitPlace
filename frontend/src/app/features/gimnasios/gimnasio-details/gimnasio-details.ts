import { Component, computed, inject, input, output } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { gimnasioResponse } from '../../../core/gimnasios/gimnasios';
import { Usuario } from '../../../core/usuario/usuario';

@Component({
  imports: [],
  selector: 'app-gimnasio-details',
  styleUrl: './gimnasio-details.css',
  templateUrl: './gimnasio-details.html',
})
export class GimnasioDetails {
  private readonly usuarioService = inject(Usuario);

  gimnasio = input.required<gimnasioResponse>();
  cerrar = output<void>();
  vincular = output<void>();

  private readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly puedeVincular = computed(
    () =>
      this.perfil.hasValue() &&
      (this.perfil.value().id === this.gimnasio().idAdmin || this.perfil.value().rol === 'ADMIN') &&
      !this.gimnasio().isMercadoPagoVinculado,
  );

  onCerrar(): void {
    this.cerrar.emit();
  }
}
