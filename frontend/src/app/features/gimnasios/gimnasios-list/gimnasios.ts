import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Gimnasios, gimnasioResponse } from '../../../core/gimnasios/gimnasios';
import { GimnasioDetails } from '../gimnasio-details/gimnasio-details'; 

@Component({
  imports: [GimnasioDetails],
  selector: 'app-gimnasios',
  styleUrl: './gimnasios.css',
  templateUrl: './gimnasios.html',
})
export class GimnasioList {
  private readonly gimnasiosService = inject(Gimnasios);
  private readonly router = inject(Router);

  // Lee de la URL el parámetro "mp" (ok, error o null) para saber cómo terminó la vinculación con Mercado Pago.
  protected readonly resultadoMp = inject(ActivatedRoute).snapshot.queryParamMap.get('mp');

  protected readonly pagina = signal(0);

  protected readonly gimnasios = rxResource({
    params: () => this.pagina(),
    stream: ({ params }) => this.gimnasiosService.getAllGimnasios(params),
  });

  protected readonly misGimnasios = rxResource({
    stream: () => this.gimnasiosService.getMisGimnasios(),
  });

  protected readonly tieneGimnasiosPropios = computed(
    () => this.misGimnasios.hasValue() && this.misGimnasios.value().length > 0,
  );

  protected readonly gimnasioSeleccionado = signal<gimnasioResponse | null>(null);
  protected readonly errorVinculacion = signal(false);

  // Si la URL trae "?mp=...", lo borra sin sumar otra entrada al historial, así el aviso no vuelve a salir al recargar.
  constructor() {
    if (this.resultadoMp) {
      this.router.navigate([], { queryParams: { mp: null }, queryParamsHandling: 'merge', replaceUrl: true });
    }
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
    this.gimnasioSeleccionado.set(gimnasio);
  }

  protected cerrarDetalle(): void {
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
}
