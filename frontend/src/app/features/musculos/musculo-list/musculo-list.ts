import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  etiquetaGrupoMuscular,
  GRUPOS_MUSCULARES,
  MusculoRequest,
  MusculoService,
} from '../../../core/services/musculo-service';
import { Musculo } from '../../../model/musculo';
import { MusculoForm } from '../musculo-form/musculo-form';

@Component({
  selector: 'app-musculo-list',
  imports: [CommonModule, FormsModule, MusculoForm],
  templateUrl: './musculo-list.html',
  styleUrl: './musculo-list.css',
})
export class MusculoList implements OnInit {
  private musculoService = inject(MusculoService);

  musculos = signal<Musculo[]>([]);
  cargando = signal<boolean>(false);
  busqueda = signal<string>('');
  filtroGrupo = signal<string>('');

  grupos = GRUPOS_MUSCULARES;

  formularioAbierto = signal<boolean>(false);
  musculoAEditar = signal<Musculo | null>(null);
  musculoAEliminar = signal<Musculo | null>(null);
  guardandoMusculo = signal<boolean>(false);
  eliminandoMusculo = signal<boolean>(false);
  errorFormulario = signal<string | null>(null);

  mensajeExito = signal<string | null>(null);
  mensajeError = signal<string | null>(null);

  musculosFiltrados = computed(() => {
    const termino = this.busqueda().toLowerCase().trim();
    const grupo = this.filtroGrupo();

    return this.musculos().filter((m) => {
      const coincideGrupo = !grupo || m.grupoMuscular === grupo;
      if (!coincideGrupo) return false;

      if (!termino) return true;

      const coincideNombre = m.nombre.toLowerCase().includes(termino);
      const coincideFuncion = m.funcionAnatomica?.toLowerCase().includes(termino) ?? false;
      const coincideEtiquetaGrupo = this.etiquetaGrupo(m.grupoMuscular)
        .toLowerCase()
        .includes(termino);

      return coincideNombre || coincideFuncion || coincideEtiquetaGrupo;
    });
  });

  ngOnInit(): void {
    this.cargarMusculos();
  }

  cargarMusculos(): void {
    this.cargando.set(true);
    this.musculoService.getAllMusculos().subscribe({
      next: (data) => {
        this.musculos.set(data.content);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error('Error al cargar músculos:', err);
        this.cargando.set(false);
      },
    });
  }

  etiquetaGrupo(grupo: string): string {
    return etiquetaGrupoMuscular(grupo);
  }

  abrirNuevo(): void {
    this.musculoAEditar.set(null);
    this.errorFormulario.set(null);
    this.formularioAbierto.set(true);
  }

  abrirEditar(musculo: Musculo, event?: Event): void {
    event?.stopPropagation();
    this.musculoAEditar.set(musculo);
    this.errorFormulario.set(null);
    this.formularioAbierto.set(true);
  }

  cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.musculoAEditar.set(null);
    this.errorFormulario.set(null);
  }

  guardarMusculo(payload: MusculoRequest): void {
    this.guardandoMusculo.set(true);
    this.errorFormulario.set(null);

    const mActual = this.musculoAEditar();
    if (mActual) {
      this.musculoService.actualizarMusculo(mActual.id, payload).subscribe({
        next: (actualizado) => {
          this.musculos.update((lista) =>
            lista.map((item) => (item.id === actualizado.id ? actualizado : item))
          );
          this.guardandoMusculo.set(false);
          this.cerrarFormulario();
          this.mostrarExito(`Músculo "${actualizado.nombre}" actualizado correctamente.`);
        },
        error: (err) => {
          this.guardandoMusculo.set(false);
          const msg =
            err.error?.message || err.error?.error || 'No se pudo actualizar el músculo.';
          this.errorFormulario.set(msg);
        },
      });
    } else {
      this.musculoService.crearMusculo(payload).subscribe({
        next: (creado) => {
          this.musculos.update((lista) => [...lista, creado]);
          this.guardandoMusculo.set(false);
          this.cerrarFormulario();
          this.mostrarExito(`Músculo "${creado.nombre}" creado exitosamente.`);
        },
        error: (err) => {
          this.guardandoMusculo.set(false);
          const msg =
            err.error?.message || err.error?.error || 'No se pudo crear el músculo.';
          this.errorFormulario.set(msg);
        },
      });
    }
  }

  abrirEliminar(musculo: Musculo, event?: Event): void {
    event?.stopPropagation();
    this.musculoAEliminar.set(musculo);
  }

  cerrarEliminar(): void {
    this.musculoAEliminar.set(null);
  }

  confirmarEliminar(): void {
    const m = this.musculoAEliminar();
    if (!m || this.eliminandoMusculo()) return;

    this.eliminandoMusculo.set(true);
    this.musculoService.eliminarMusculo(m.id).subscribe({
      next: () => {
        this.eliminandoMusculo.set(false);
        this.musculos.update((lista) => lista.filter((item) => item.id !== m.id));
        this.cerrarEliminar();
        this.mostrarExito(`Músculo "${m.nombre}" eliminado del catálogo.`);
      },
      error: (err) => {
        this.eliminandoMusculo.set(false);
        this.cerrarEliminar();
        const msg =
          err.error?.message ||
          err.error?.error ||
          'No se pudo eliminar el músculo porque está asignado a uno o más ejercicios.';
        this.mostrarError(msg);
      },
    });
  }

  private mostrarExito(mensaje: string): void {
    this.mensajeExito.set(mensaje);
    setTimeout(() => this.mensajeExito.set(null), 4000);
  }

  private mostrarError(mensaje: string): void {
    this.mensajeError.set(mensaje);
    setTimeout(() => this.mensajeError.set(null), 6000);
  }
}
