import { Component, DOCUMENT, DestroyRef, computed, inject, input, output, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { gimnasioResponse } from '../../../core/gimnasios/gimnasios';
import { Usuario } from '../../../core/usuario/usuario';
import { SuscripcionGimnasioResponse } from '../../../core/suscripciones/suscripciones';

@Component({
  imports: [],
  selector: 'app-gimnasio-details',
  styleUrl: './gimnasio-details.css',
  templateUrl: './gimnasio-details.html',
  host: { '(document:keydown.escape)': 'onCerrar()' },
})
export class GimnasioDetails {
  private readonly usuarioService = inject(Usuario);
  private readonly document = inject(DOCUMENT);

  gimnasio = input.required<gimnasioResponse>();
  suscripcion = input<SuscripcionGimnasioResponse | null | undefined>(undefined);
  pagando = input(false);
  errorPago = input(false);
  cerrar = output<void>();
  suscribir = output<boolean>();
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

  protected readonly puedeSuscribir = computed(
    () => this.perfil.hasValue() && (this.perfil.value().id !== this.gimnasio().idAdmin));

  protected readonly accionSuscripcion = computed<'suscribirse' | 'completar-pago' | 'miembro' | 'otro-gimnasio' | null>(() => {
    const suscripcion = this.suscripcion();
    if (suscripcion === undefined) return null;
    if (suscripcion === null) return 'suscribirse';
    if (suscripcion.idGimnasio !== this.gimnasio().id) return 'otro-gimnasio';
    return suscripcion.estadoSuscripcion === 'ACTIVA' ? 'miembro' : 'completar-pago';
  });

  protected readonly confirmandoGimnasio = signal(false);

  protected readonly pendiente = computed(() => this.puedeSuscribir() && this.accionSuscripcion() === 'completar-pago');
  protected readonly alDia = computed(() => this.puedeSuscribir() && this.accionSuscripcion() === 'miembro');
  protected readonly mostrarPago = computed(
    () => this.puedeSuscribir() && (this.accionSuscripcion() === 'suscribirse' || this.pendiente()),
  );

  protected readonly mes = new Intl.DateTimeFormat('es-AR', { month: 'long' }).format(new Date());

  protected readonly precio = computed(() => new Intl.NumberFormat('es-AR').format(this.gimnasio().precioCuota));

  protected readonly dias = computed(() => this.gimnasio().diasDeApertura.map((dia) => dia.slice(0, 3)).join(', '));

  constructor() {
    this.document.body.style.overflow = 'hidden';
    inject(DestroyRef).onDestroy(() => (this.document.body.style.overflow = ''));
  }

  onCerrar(): void {
    this.cerrar.emit();
  }
}
