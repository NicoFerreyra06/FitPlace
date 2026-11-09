import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Gimnasios, gimnasioResponse } from '../../../core/gimnasios/gimnasios';
import { GimnasioDetails } from '../gimnasio-details/gimnasio-details';
import { MiSuscripcion } from '../mi-suscripcion/mi-suscripcion';
import { Suscripciones } from '../../../core/suscripciones/suscripciones';

@Component({
  imports: [GimnasioDetails, MiSuscripcion, RouterLink],
  selector: 'app-gimnasios',
  styleUrl: './gimnasios.css',
  templateUrl: './gimnasios.html',
})
export class GimnasioList {
  private readonly gimnasiosService = inject(Gimnasios);
  private readonly suscripcionService = inject(Suscripciones);

  private readonly router = inject(Router);

  // Lee de la URL el parámetro "mp" (ok, error o null) para saber cómo terminó la vinculación con Mercado Pago.
  protected readonly resultadoMp = inject(ActivatedRoute).snapshot.queryParamMap.get('mp');
  private readonly estadoPago = inject(ActivatedRoute).snapshot.queryParamMap.get('status');

  protected readonly pagina = signal(0);

  protected readonly gimnasios = rxResource({
    params: () => this.pagina(),
    stream: ({ params }) => this.gimnasiosService.getAllGimnasios(params),
  });

  protected readonly misGimnasios = rxResource({
    stream: () => this.gimnasiosService.getMisGimnasios(),
  });

  protected readonly miSuscripcion = rxResource({
    stream: () => this.suscripcionService.getSuscripcion(),
  });

  protected readonly suscripcionActual = computed(() => {
    if (this.miSuscripcion.isLoading()) return undefined;
    if (this.miSuscripcion.hasValue()) return this.miSuscripcion.value();
    const error = this.miSuscripcion.error() as HttpErrorResponse | undefined;
    return error?.status === 404 ? null : undefined;
  });

  protected readonly tieneGimnasiosPropios = computed(
    () => this.misGimnasios.hasValue() && this.misGimnasios.value().length > 0,
  );

  protected readonly gimnasioSeleccionado = signal<gimnasioResponse | null>(null);
  protected readonly errorVinculacion = signal(false);

  protected readonly errorSuscripcion = signal(false);
  protected readonly avisoSuscripcion = signal<string | null>(null);
  protected readonly pagandoMp = signal(false);
  protected readonly errorPagoMp = signal(false);

  // Si la URL trae "?mp=...", lo borra sin sumar otra entrada al historial, así el aviso no vuelve a salir al recargar.
  constructor() {
    if (this.resultadoMp) {
      this.router.navigate([], { queryParams: { mp: null }, queryParamsHandling: 'merge', replaceUrl: true });
    }

    if (this.estadoPago) {
      this.router.navigate([], { replaceUrl: true });
      this.mostrarResultadoPago();
    }
  }

  private mostrarResultadoPago(): void {
    if (!['approved', 'pending', 'in_process'].includes(this.estadoPago ?? '')) {
      this.errorSuscripcion.set(true);
      this.avisoSuscripcion.set('El pago no se completó. Podés intentarlo de nuevo desde el gimnasio.');
      return;
    }

    this.suscripcionService.getSuscripcion().subscribe({
      next: (suscripcion) => {
        if (suscripcion.estadoSuscripcion === 'ACTIVA') {
          this.avisoSuscripcion.set(`¡Listo! Ya sos parte de ${suscripcion.nombreGimnasio}.`);
        } else {
          this.avisoSuscripcion.set(`Tu pago a ${suscripcion.nombreGimnasio} se está procesando. En unos minutos se activa tu suscripción.`);
        }
      },
      error: () => this.avisoSuscripcion.set('Tu pago se está procesando. En unos minutos se activa tu suscripción.'),
    });
  }

  protected get cargando(): boolean {
    return this.gimnasios.isLoading() && !this.gimnasios.hasValue();
  }

  protected get hayError(): boolean {
    return Boolean(this.gimnasios.error());
  }

  protected reintentar(): void {
    this.gimnasios.reload();
  }

  protected verDetalle(gimnasio: gimnasioResponse): void {
    this.pagandoMp.set(false);
    this.errorPagoMp.set(false);
    this.gimnasioSeleccionado.set(gimnasio);
  }

  protected cerrarDetalle(): void {
    this.pagandoMp.set(false);
    this.gimnasioSeleccionado.set(null);
  }

  protected cambiarPagina(delta: number): void {
    this.pagina.update((p) => p + delta);
  }

  protected vincular(idGimnasio: number): void {
    this.errorVinculacion.set(false);
    this.gimnasiosService.getUrlVinculacion(idGimnasio).subscribe({
      next: (url) => {
        window.location.href = url;
      },
      error: () => {
        this.cerrarDetalle();
        this.errorVinculacion.set(true);
      },
    });
  }

  protected suscribirse(gimnasio: gimnasioResponse, conMercadoPago: boolean): void{
    this.errorSuscripcion.set(false);
    this.avisoSuscripcion.set(null);
    this.errorPagoMp.set(false);
    this.pagandoMp.set(conMercadoPago);

    this.suscripcionService.getSuscripcion().subscribe({
      next: (data) => {
        if (data.estadoSuscripcion === 'ACTIVA'){
          this.errorSuscripcion.set(true);
          this.avisoSuscripcion.set(`Ya tenés una suscripción activa en ${data.nombreGimnasio}.`)
          this.cerrarDetalle()
        } else if (data.estadoSuscripcion === 'PENDIENTE'){
          if(data.idGimnasio !== gimnasio.id){
            this.errorSuscripcion.set(true);
            this.avisoSuscripcion.set(`Ya tenés una suscripción pendiente en ${data.nombreGimnasio}.`)
            this.cerrarDetalle()
          } else {
            this.pagar(gimnasio, data.id, conMercadoPago);
          }
        }
      },
      error: (err) => {
        if (err.status === 404){
          this.suscripcionService.createSuscripcion(gimnasio.id).subscribe({
            next: (data) => this.pagar(gimnasio, data.id, conMercadoPago),
            error: () => this.fallarSuscripcion('No se pudo crear la suscripción. Intentá de nuevo.', conMercadoPago),
          });
        } else {
          this.fallarSuscripcion('No se pudo procesar la suscripción. Intentá de nuevo.', conMercadoPago);
        }
      }
    })
  }

  private pagar (gimnasio: gimnasioResponse, idSuscripcion: number, conMercadoPago: boolean): void{
    if(conMercadoPago){
      this.suscripcionService.generarPago(idSuscripcion).subscribe({
        next: (url) => {
        window.location.href = url;
        },
        error: () => this.fallarSuscripcion('No se pudo iniciar el pago. Intentá de nuevo.', true),
      })
    } else {
      this.cerrarDetalle();
      this.miSuscripcion.reload();
      this.avisoSuscripcion.set(`Te anotaste en ${gimnasio.nombre}. Pagá la cuota en recepción para que te activen.`)
    }
  }

  protected completarPago(idSuscripcion: number): void {
    this.errorSuscripcion.set(false);
    this.avisoSuscripcion.set(null);
    this.pagandoMp.set(true);
    this.suscripcionService.generarPago(idSuscripcion).subscribe({
      next: (url) => {
        window.location.href = url;
      },
      error: () => {
        this.pagandoMp.set(false);
        this.errorSuscripcion.set(true);
        this.avisoSuscripcion.set('No se pudo iniciar el pago. Intentá de nuevo.');
      },
    });
  }

  protected cancelarSuscripcion(idSuscripcion: number): void {
    this.errorSuscripcion.set(false);
    this.avisoSuscripcion.set(null);

    this.suscripcionService.cancelarSuscripcion(idSuscripcion).subscribe({
      next: () => {
        this.miSuscripcion.reload();
        this.avisoSuscripcion.set('Cancelaste tu suscripción correctamente')
        },
      error: () => {
          this.miSuscripcion.reload();
          this.avisoSuscripcion.set('Error al desuscribir')
          this.errorSuscripcion.set(true);
        }
    })

  }

  private fallarSuscripcion(texto: string, conMercadoPago: boolean): void {
    this.miSuscripcion.reload();
    if (conMercadoPago) {
      this.pagandoMp.set(false);
      this.errorPagoMp.set(true);
      return;
    }
    this.cerrarDetalle();
    this.errorSuscripcion.set(true);
    this.avisoSuscripcion.set(texto);
  }
}
