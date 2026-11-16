import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { GananciaGimnasio, Usuario } from '../../../core/usuario/usuario';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-ganancias-admin',
  templateUrl: './ganancias-admin.html',
  styleUrl: './ganancias-admin.css',
})
export class GananciasAdmin {
  private readonly usuarioService = inject(Usuario);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly formulario = this.formBuilder.nonNullable.group({
    desde: ['', Validators.required],
    hasta: ['', Validators.required],
  });
  protected readonly resultados = signal<GananciaGimnasio[] | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly errorRango = signal<string | null>(null);
  protected readonly formatoMoneda = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  protected readonly totalIngresosBrutos = computed(() =>
    this.totalizar(this.resultados(), (resultado) => resultado.totalIngresosBrutos),
  );
  protected readonly totalComision = computed(() =>
    this.totalizar(this.resultados(), (resultado) => resultado.comisionFitPlace),
  );
  protected readonly totalGananciaGimnasios = computed(() =>
    this.totalizar(this.resultados(), (resultado) => resultado.gananciaNetaGimnasio),
  );

  protected consultar(): void {
    this.error.set(null);
    this.errorRango.set(null);
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid) {
      this.resultados.set(null);
      return;
    }

    const { desde, hasta } = this.formulario.getRawValue();
    if (desde > hasta) {
      this.resultados.set(null);
      this.errorRango.set('La fecha desde no puede ser posterior a la fecha hasta.');
      return;
    }

    this.cargando.set(true);
    this.resultados.set(null);
    this.usuarioService.getGananciasPorGimnasio(desde, hasta).subscribe({
      next: (resultados) => {
        this.resultados.set(resultados);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las ganancias. Verificá el rango e intentá de nuevo.');
        this.cargando.set(false);
      },
    });
  }

  private totalizar(
    resultados: GananciaGimnasio[] | null,
    obtenerValor: (resultado: GananciaGimnasio) => number,
  ): number {
    if (!resultados) return 0;
    const total = resultados.reduce((acumulado, resultado) => acumulado + obtenerValor(resultado), 0);
    return Math.round((total + Number.EPSILON) * 100) / 100;
  }
}
