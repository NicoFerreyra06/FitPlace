import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Gimnasios } from '../../../core/gimnasios/gimnasios';
import { estadoSuscripcion, SuscripcionGimnasioResponse, Suscripciones } from '../../../core/suscripciones/suscripciones';

@Component({
  imports: [DatePipe, FormsModule, RouterLink],
  selector: 'app-gestion-gimnasio',
  styleUrl: './gestion-gimnasio.css',
  templateUrl: './gestion-gimnasio.html',
})
export class GestionGimnasio {
  private readonly gimnasiosService = inject(Gimnasios);
  private readonly suscripcionService = inject(Suscripciones);
  private readonly idGimnasio = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly estados: estadoSuscripcion[] = ['PENDIENTE', 'ACTIVA', 'VENCIDA', 'CANCELADA'];
  
  protected readonly estado = signal<estadoSuscripcion | null>(null);
  protected readonly activandoId = signal<number | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly error = signal(false);

  protected readonly gimnasio = rxResource({
    stream: () => this.gimnasiosService.getGimnasio(this.idGimnasio),
  });

  protected readonly pendientes = rxResource({
    stream: () => this.suscripcionService.getSuscripcionesGimnasio(this.idGimnasio, 'PENDIENTE'),
  });

  protected readonly suscripciones = rxResource({
    params: () => this.estado(),
    stream: ({ params }) => this.suscripcionService.getSuscripcionesGimnasio(this.idGimnasio, params),
  });

  protected readonly miembros = rxResource({
    stream: () => this.gimnasiosService.getMiembros(this.idGimnasio),
  });

  protected readonly sinAcceso = computed(() => {
    const error = this.miembros.error() as HttpErrorResponse | undefined;
    if (error?.status !== 403 && error?.status !== 404) return null;
    return error.error?.message ?? 'No tenés acceso a este gimnasio.';
  });

  protected readonly cantidadMiembros = computed(() => this.miembros.value()?.length ?? 0);

  protected readonly precio = computed(() =>
    this.gimnasio.hasValue() ? new Intl.NumberFormat('es-AR').format(this.gimnasio.value().precioCuota) : '',
  );

  protected readonly busqueda = signal<string>('');

  protected readonly miembrosFiltrados = computed(() => {
    const lista = this.miembros.value() ?? [];
    const termino = this.busqueda().trim().toLowerCase();

    if (!termino) {
      return lista;
    }

    return lista.filter((miembro) => miembro.username.toLowerCase().includes(termino));
  });

  protected activar(suscripcion: SuscripcionGimnasioResponse, metodoPago: 'EFECTIVO' | 'TRANSFERENCIA'): void {
    this.activandoId.set(suscripcion.id);
    this.aviso.set(null);

    this.suscripcionService.activarSuscripcion(suscripcion.id, metodoPago).subscribe({
      next: () => {
        this.activandoId.set(null);
        this.error.set(false);
        this.aviso.set(`Activaste la suscripción de ${suscripcion.nombreUsuario}.`);
        this.pendientes.reload();
        this.suscripciones.reload();
        this.miembros.reload();
      },
      error: (err) => {
        this.activandoId.set(null);
        this.error.set(true);
        this.aviso.set(err.error?.message ?? 'No se pudo activar la suscripción.');
      },
    });
  }
}
