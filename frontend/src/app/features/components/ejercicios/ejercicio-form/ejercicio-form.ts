import { Component, computed, effect, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Ejercicio } from '../../../../model/ejercicio';
import { Musculo } from '../../../../model/musculo';
import { EjercicioRequest } from '../../../../core/services/ejercicio-service';

const ORDEN_GRUPOS: Record<string, number> = {
  PECHO: 1,
  DORSAL: 2,
  TRAPECIO: 3,
  HOMBRO_ANTERIOR: 4,
  HOMBRO_LATERAL: 5,
  HOMBRO_POSTERIOR: 6,
  BICEPS: 7,
  TRICEPS: 8,
  ANTEBRAZOS: 9,
  CORE: 10,
  CUADRICEPS: 11,
  ISQUIOTIBIALES: 12,
  GLUTEOS: 13,
  ADUCTORES: 14,
  GEMELOS: 15,
};

@Component({
  selector: 'app-ejercicio-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './ejercicio-form.html',
  styleUrl: './ejercicio-form.css',
})
export class EjercicioForm implements OnInit {
  ejercicio = input<Ejercicio | null>(null);
  musculos = input<Musculo[]>([]);
  guardando = input<boolean>(false);
  errorExterno = input<string | null>(null);

  cerrar = output<void>();
  guardar = output<EjercicioRequest>();

  nombre = signal<string>('');
  descripcion = signal<string>('');
  principalesSeleccionados = signal<Set<number>>(new Set());
  secundariosSeleccionados = signal<Set<number>>(new Set());
  errorLocal = signal<string | null>(null);

  esEdicion = computed(() => !!this.ejercicio());

  musculosOrdenados = computed<Musculo[]>(() => {
    return [...this.musculos()].sort((a, b) => {
      const ordenA = ORDEN_GRUPOS[a.grupoMuscular] ?? 99;
      const ordenB = ORDEN_GRUPOS[b.grupoMuscular] ?? 99;
      if (ordenA !== ordenB) {
        return ordenA - ordenB;
      }
      return a.nombre.localeCompare(b.nombre);
    });
  });

  ngOnInit(): void {
    const ej = this.ejercicio();
    if (ej) {
      this.nombre.set(ej.nombre);
      this.descripcion.set(ej.descripcion || '');
      this.principalesSeleccionados.set(
        new Set(ej.musculosPrincipales?.map((m) => Number(m.id)) ?? [])
      );
      this.secundariosSeleccionados.set(
        new Set(ej.musculosSecundarios?.map((m) => Number(m.id)) ?? [])
      );
    }
  }

  esPrincipal(id: string | number): boolean {
    return this.principalesSeleccionados().has(Number(id));
  }

  esSecundario(id: string | number): boolean {
    return this.secundariosSeleccionados().has(Number(id));
  }

  togglePrincipal(id: string | number): void {
    const numId = Number(id);
    const nuevosPrincipales = new Set(this.principalesSeleccionados());
    const nuevosSecundarios = new Set(this.secundariosSeleccionados());

    if (nuevosPrincipales.has(numId)) {
      nuevosPrincipales.delete(numId);
    } else {
      nuevosPrincipales.add(numId);
      // No puede ser secundario a la vez
      nuevosSecundarios.delete(numId);
    }

    this.principalesSeleccionados.set(nuevosPrincipales);
    this.secundariosSeleccionados.set(nuevosSecundarios);
    this.errorLocal.set(null);
  }

  toggleSecundario(id: string | number): void {
    const numId = Number(id);
    const nuevosPrincipales = new Set(this.principalesSeleccionados());
    const nuevosSecundarios = new Set(this.secundariosSeleccionados());

    if (nuevosSecundarios.has(numId)) {
      nuevosSecundarios.delete(numId);
    } else {
      nuevosSecundarios.add(numId);
      // No puede ser principal a la vez
      nuevosPrincipales.delete(numId);
    }

    this.principalesSeleccionados.set(nuevosPrincipales);
    this.secundariosSeleccionados.set(nuevosSecundarios);
    this.errorLocal.set(null);
  }

  onGuardar(): void {
    const nombreTrim = this.nombre().trim();
    if (!nombreTrim) {
      this.errorLocal.set('El nombre del ejercicio es obligatorio.');
      return;
    }

    if (this.principalesSeleccionados().size === 0) {
      this.errorLocal.set('Debes seleccionar al menos un músculo principal.');
      return;
    }

    const payload: EjercicioRequest = {
      nombre: nombreTrim,
      descripcion: this.descripcion().trim() || undefined,
      musculoPrincipalId: Array.from(this.principalesSeleccionados()),
      musculoSecundarioId: Array.from(this.secundariosSeleccionados()),
    };

    this.guardar.emit(payload);
  }

  onCerrar(): void {
    this.cerrar.emit();
  }
}
