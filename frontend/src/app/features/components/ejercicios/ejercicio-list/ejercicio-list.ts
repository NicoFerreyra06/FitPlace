import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { EjercicioService } from '../../../../core/services/ejercicio-service';
import { Ejercicio } from '../../../../model/ejercicio';
import { EjercicioDetails } from '../ejercicio-details/ejercicio-details';
import { FormsModule } from '@angular/forms';
import { MusculoService } from '../../../../core/services/musculo-service';
import { Musculo } from '../../../../model/musculo';

@Component({
  imports: [EjercicioDetails, FormsModule],
  selector: 'app-ejercicio-list',
  styleUrl: './ejercicio-list.css',
  templateUrl: './ejercicio-list.html',
})
export class EjercicioList implements OnInit {
  private ejercicioService = inject(EjercicioService);
  private musculoService = inject(MusculoService);

  ejercicios = signal<Ejercicio[]>([]);
  musculos = signal<Musculo[]>([]);

  ejercicioSeleccionado = signal<Ejercicio | null>(null);
  cargando = signal<boolean>(false);
  busqueda = signal<string>('');

  ejerciciosFiltrados = computed(() => {
    const termino = this.busqueda().toLowerCase().trim();
    if (!termino) {
      return this.ejercicios();
    }
    return this.ejercicios().filter(ej => {
      const coincideNombre = ej.nombre.toLowerCase().includes(termino);
      const coincideMusculo = ej.musculosPrincipales.some(m =>
        m.nombre.toLowerCase().includes(termino) ||
        m.grupoMuscular.toLowerCase().includes(termino)
      ) || ej.musculosSecundarios.some(m =>
        m.nombre.toLowerCase().includes(termino) ||
        m.grupoMuscular.toLowerCase().includes(termino)
      );
      return coincideNombre || coincideMusculo;
    });
  });

  ngOnInit(): void {
    this.ejercicioService.getAllEjercicio().subscribe({
      next: (data) => {
        this.ejercicios.set(data.content);
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
      },
    });

    this.musculoService.getAllMusculos().subscribe({
      next: (data) => {
        this.musculos.set(data.content);
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
      },
    });
  }

  filtrarPorMusculo(musculoId: string): void {
    if (!musculoId) {
      this.ejercicioService.getAllEjercicio().subscribe({
        next: (data) => {
          this.ejercicios.set(data.content);
        },
      });
    } else {
      this.ejercicioService.getEjerciciosPorMusculo(musculoId).subscribe({
        next: (data) => {
          this.ejercicios.set(data);
        },
      });
    }
  }

  verDetalle(ejercicio: Ejercicio): void {
    this.ejercicioSeleccionado.set(ejercicio);
  }

  cerrarDetalle(): void {
    this.ejercicioSeleccionado.set(null);
  }
}
