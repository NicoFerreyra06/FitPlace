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
  cerrar = output<void>();

  onCerrar(): void {
    this.cerrar.emit();
  }
}
