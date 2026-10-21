import { Component, inject, OnInit, signal } from '@angular/core';
import { EjercicioService } from '../../../../core/services/ejercicio-service';
import { Ejercicio } from '../../../../model/ejercicio';
import { EjercicioDetails } from '../ejercicio-details/ejercicio-details';
import { MusculoService } from '../../../../core/services/musculo-service';
import { Musculo } from '../../../../model/musculo';

@Component({
  imports: [EjercicioDetails],
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
