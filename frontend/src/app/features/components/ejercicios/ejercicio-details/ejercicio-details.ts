import { Component, input, output } from '@angular/core';
import { Ejercicio } from '../../../../model/ejercicio';

@Component({
  imports: [],
  selector: 'app-ejercicio-details',
  styleUrl: './ejercicio-details.css',
  templateUrl: './ejercicio-details.html',
})
export class EjercicioDetails {
  ejercicio = input.required<Ejercicio>();
  esAdmin = input<boolean>(false);
  cerrar = output<void>();
  editar = output<Ejercicio>();
  eliminar = output<Ejercicio>();

  onCerrar(): void {
    this.cerrar.emit();
  }

  onEditar(): void {
    this.editar.emit(this.ejercicio());
  }

  onEliminar(): void {
    this.eliminar.emit(this.ejercicio());
  }
}
