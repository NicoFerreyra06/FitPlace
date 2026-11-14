import { Component, computed, input } from '@angular/core';

/**
 * Tarjetas de racha actual y máxima. La usan "Mi perfil" y el perfil de un amigo.
 *
 * El host usa `display: contents`, así que las dos tarjetas se acomodan como hijas directas
 * del contenedor flex del padre. Para darles un alto mínimo, el padre define
 * `--rachas-alto-min` en su contenedor.
 */
@Component({
  selector: 'app-rachas',
  styleUrl: './rachas.css',
  templateUrl: './rachas.html',
})
export class Rachas {
  readonly actual = input<number | null>(null);
  readonly maxima = input<number | null>(null);

  /** El backend manda null cuando el usuario todavía no entrenó: se muestra como 0. */
  protected readonly diasActual = computed(() => this.actual() ?? 0);
  protected readonly diasMaxima = computed(() => this.maxima() ?? 0);

  protected unidad(dias: number): string {
    return dias === 1 ? 'día' : 'días';
  }
}
