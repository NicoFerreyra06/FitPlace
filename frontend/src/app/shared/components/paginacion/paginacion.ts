import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-paginacion',
  styleUrl: './paginacion.css',
  templateUrl: './paginacion.html',
})
export class Paginacion {
  readonly pagina = input.required<number>();
  readonly totalPaginas = input.required<number>();

  readonly cambiar = output<number>();

  protected readonly esPrimera = computed(() => this.pagina() <= 0);
  protected readonly esUltima = computed(() => this.pagina() >= this.totalPaginas() - 1);}
