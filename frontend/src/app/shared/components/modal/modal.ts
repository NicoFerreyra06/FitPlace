import { Component, input, output } from '@angular/core';

/**
 * Marco común de todos los modales: fondo con blur, cabecera con título y ✕.
 * Se cierra con Escape, con la ✕ o haciendo click afuera. El contenido entra por <ng-content>.
 */
@Component({
  selector: 'app-modal',
  templateUrl: './modal.html',
  styleUrl: './modal.css',
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
})
export class Modal {
  readonly titulo = input.required<string>();
  readonly ancho = input('520px');
  readonly cerrar = output<void>();
}
