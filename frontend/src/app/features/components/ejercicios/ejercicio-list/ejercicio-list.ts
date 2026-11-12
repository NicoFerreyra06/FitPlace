import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { EjercicioService, EjercicioRequest } from '../../../../core/services/ejercicio-service';
import { Usuario } from '../../../../core/usuario/usuario';
import { Ejercicio } from '../../../../model/ejercicio';
import { EjercicioDetails } from '../ejercicio-details/ejercicio-details';
import { EjercicioForm } from '../ejercicio-form/ejercicio-form';
import { FormsModule } from '@angular/forms';
import { MusculoService } from '../../../../core/services/musculo-service';
import { Musculo } from '../../../../model/musculo';

@Component({
  imports: [EjercicioDetails, EjercicioForm, FormsModule],
  selector: 'app-ejercicio-list',
  styleUrl: './ejercicio-list.css',
  templateUrl: './ejercicio-list.html',
})
export class EjercicioList implements OnInit {
  private ejercicioService = inject(EjercicioService);
  private musculoService = inject(MusculoService);
  private usuarioService = inject(Usuario);

  ejercicios = signal<Ejercicio[]>([]);
  musculos = signal<Musculo[]>([]);

  ejercicioSeleccionado = signal<Ejercicio | null>(null);
  cargando = signal<boolean>(false);
  busqueda = signal<string>('');

  // Estado del usuario y rol administrador
  protected readonly perfilActivo = toSignal(
    this.usuarioService.getPerfilGuardado().pipe(catchError(() => of(null))),
    { initialValue: null }
  );
  protected readonly esAdmin = computed(() => this.perfilActivo()?.rol === 'ADMIN');

  // Estado para gestión admin
  formularioAbierto = signal<boolean>(false);
  ejercicioAEditar = signal<Ejercicio | null>(null);
  ejercicioAEliminar = signal<Ejercicio | null>(null);
  guardandoEjercicio = signal<boolean>(false);
  errorFormulario = signal<string | null>(null);
  mensajeExito = signal<string | null>(null);
  mensajeError = signal<string | null>(null);

  ejerciciosFiltrados = computed(() => {
    const termino = this.busqueda().toLowerCase().trim();
    if (!termino) {
      return this.ejercicios();
    }
    return this.ejercicios().filter((ej) => {
      const coincideNombre = ej.nombre.toLowerCase().includes(termino);
      const coincideMusculo =
        ej.musculosPrincipales?.some(
          (m) =>
            m.nombre.toLowerCase().includes(termino) ||
            m.grupoMuscular?.toLowerCase().includes(termino)
        ) ||
        ej.musculosSecundarios?.some(
          (m) =>
            m.nombre.toLowerCase().includes(termino) ||
            m.grupoMuscular?.toLowerCase().includes(termino)
        );
      return coincideNombre || coincideMusculo;
    });
  });

  protected readonly pagina = signal(0);
  protected readonly tamanioPagina = 9;

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.ejerciciosFiltrados().length / this.tamanioPagina)),
  );

  protected readonly paginaActual = computed(() =>
    Math.min(this.pagina(), this.totalPaginas() - 1),
  );

  protected readonly ejerciciosPagina = computed(() => {
    const inicio = this.paginaActual() * this.tamanioPagina;
    return this.ejerciciosFiltrados().slice(inicio, inicio + this.tamanioPagina);
  });

  ngOnInit(): void {
    this.cargarEjercicios();
    this.cargarMusculos();
  }

  cargarEjercicios(): void {
    this.cargando.set(true);
    this.ejercicioService.getAllEjercicio().subscribe({
      next: (data) => {
        this.ejercicios.set(data.content);
        this.pagina.set(0);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
        this.cargando.set(false);
      },
    });
  }

  cargarMusculos(): void {
    this.musculoService.getAllMusculos().subscribe({
      next: (data) => {
        this.musculos.set(data.content);
      },
      error: (err) => {
        console.error('Error al cargar músculos:', err);
      },
    });
  }

  filtrarPorMusculo(musculoId: string): void {
    this.pagina.set(0);
    if (!musculoId) {
      this.cargarEjercicios();
    } else {
      this.ejercicioService.getEjerciciosPorMusculo(musculoId).subscribe({
        next: (data) => {
          this.ejercicios.set(data);
          this.pagina.set(0);
        },
      });
    }
  }

  protected buscar(valor: string): void {
    this.busqueda.set(valor);
    this.pagina.set(0);
  }

  protected cambiarPagina(delta: number): void {
    this.pagina.update((actual) =>
      Math.min(Math.max(actual + delta, 0), this.totalPaginas() - 1),
    );
  }

  verDetalle(ejercicio: Ejercicio): void {
    this.ejercicioSeleccionado.set(ejercicio);
  }

  cerrarDetalle(): void {
    this.ejercicioSeleccionado.set(null);
  }

  abrirNuevoEjercicio(): void {
    this.ejercicioAEditar.set(null);
    this.errorFormulario.set(null);
    this.formularioAbierto.set(true);
  }

  abrirEditarEjercicio(ejercicio: Ejercicio, event?: Event): void {
    event?.stopPropagation();
    this.ejercicioAEditar.set(ejercicio);
    this.errorFormulario.set(null);
    this.formularioAbierto.set(true);
  }

  cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.ejercicioAEditar.set(null);
    this.errorFormulario.set(null);
  }

  guardarEjercicio(payload: EjercicioRequest): void {
    this.guardandoEjercicio.set(true);
    this.errorFormulario.set(null);

    const ejActual = this.ejercicioAEditar();
    if (ejActual) {
      this.ejercicioService.actualizarEjercicio(ejActual.id, payload).subscribe({
        next: (actualizado) => {
          this.ejercicios.update((lista) =>
            lista.map((item) => (item.id === actualizado.id ? actualizado : item))
          );
          this.guardandoEjercicio.set(false);
          this.cerrarFormulario();
          this.mostrarExito(`Ejercicio "${actualizado.nombre}" actualizado correctamente.`);
        },
        error: (err) => {
          this.guardandoEjercicio.set(false);
          const msg =
            err.error?.message || err.error?.error || 'No se pudo actualizar el ejercicio.';
          this.errorFormulario.set(msg);
        },
      });
    } else {
      this.ejercicioService.crearEjercicio(payload).subscribe({
        next: (creado) => {
          this.ejercicios.update((lista) => [creado, ...lista]);
          this.pagina.set(0);
          this.guardandoEjercicio.set(false);
          this.cerrarFormulario();
          this.mostrarExito(`Ejercicio "${creado.nombre}" creado exitosamente.`);
        },
        error: (err) => {
          this.guardandoEjercicio.set(false);
          const msg =
            err.error?.message || err.error?.error || 'No se pudo crear el ejercicio.';
          this.errorFormulario.set(msg);
        },
      });
    }
  }

  abrirEliminar(ejercicio: Ejercicio, event?: Event): void {
    event?.stopPropagation();
    this.ejercicioAEliminar.set(ejercicio);
  }

  cerrarEliminar(): void {
    this.ejercicioAEliminar.set(null);
  }

  confirmarEliminar(): void {
    const ej = this.ejercicioAEliminar();
    if (!ej) return;

    this.ejercicioService.eliminarEjercicio(ej.id).subscribe({
      next: () => {
        this.ejercicios.update((lista) => lista.filter((item) => item.id !== ej.id));
        this.cerrarEliminar();
        this.mostrarExito(`Ejercicio "${ej.nombre}" eliminado del catálogo.`);
      },
      error: (err) => {
        this.cerrarEliminar();
        const msg =
          err.error?.message || err.error?.error || 'No se pudo eliminar el ejercicio.';
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
    setTimeout(() => this.mensajeError.set(null), 5000);
  }
}
