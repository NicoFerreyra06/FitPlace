import { Component, computed, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Musculo } from '../../../model/musculo';
import { GRUPOS_MUSCULARES, MusculoRequest } from '../../../core/services/musculo-service';

@Component({
  selector: 'app-musculo-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './musculo-form.html',
  styleUrl: './musculo-form.css',
})
export class MusculoForm implements OnInit {
  musculo = input<Musculo | null>(null);
  guardando = input<boolean>(false);
  errorExterno = input<string | null>(null);

  cerrar = output<void>();
  guardar = output<MusculoRequest>();

  nombre = signal<string>('');
  grupoMuscular = signal<string>('PECHO');
  funcionAnatomica = signal<string>('');
  errorLocal = signal<string | null>(null);

  grupos = GRUPOS_MUSCULARES;

  esEdicion = computed(() => !!this.musculo());

  ngOnInit(): void {
    const m = this.musculo();
    if (m) {
      this.nombre.set(m.nombre);
      this.grupoMuscular.set(m.grupoMuscular || 'PECHO');
      this.funcionAnatomica.set(m.funcionAnatomica || '');
    }
  }

  onGuardar(): void {
    const nombreTrim = this.nombre().trim();
    if (!nombreTrim) {
      this.errorLocal.set('El nombre del músculo es obligatorio.');
      return;
    }

    if (!this.grupoMuscular()) {
      this.errorLocal.set('Debes seleccionar un grupo muscular.');
      return;
    }

    const payload: MusculoRequest = {
      nombre: nombreTrim,
      grupoMuscular: this.grupoMuscular(),
      funcionAnatomica: this.funcionAnatomica().trim() || undefined,
    };

    this.guardar.emit(payload);
  }

  onCerrar(): void {
    this.cerrar.emit();
  }
}
