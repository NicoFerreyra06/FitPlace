import { Component, computed, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SuscripcionGimnasioResponse } from '../../../core/suscripciones/suscripciones';

@Component({
  imports: [DatePipe],
  selector: 'app-mi-suscripcion',
  styleUrl: './mi-suscripcion.css',
  templateUrl: './mi-suscripcion.html',
})
export class MiSuscripcion {
  suscripcion = input.required<SuscripcionGimnasioResponse>();
  pagando = input(false);
  cancelar = output<void>();
  completarPago = output<void>();

  protected readonly confirmando = signal(false);

  protected readonly activa = computed(() => this.suscripcion().estadoSuscripcion === 'ACTIVA');

  protected readonly mes = new Intl.DateTimeFormat('es-AR', { month: 'long' }).format(new Date());

  protected readonly precio = computed(() => new Intl.NumberFormat('es-AR').format(this.suscripcion().costo));

  protected confirmarCancelacion(): void {
    this.confirmando.set(false);
    this.cancelar.emit();
  }
}
