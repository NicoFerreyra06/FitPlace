import { Component, inject, OnInit, signal } from '@angular/core';
import { EjercicioService } from '../../../../core/services/ejercicio-service';
import { Ejercicio } from '../../../../model/ejercicio';
import { EjercicioDetails } from '../ejercicio-details/ejercicio-details';

@Component({
  imports: [EjercicioDetails],
  selector: 'app-ejercicio-list',
  styleUrl: './ejercicio-list.css',
  templateUrl: './ejercicio-list.html',
})
export class EjercicioList implements OnInit {
  private ejercicioService = inject(EjercicioService);

  ejercicios = signal<Ejercicio[]>([]);
  ejercicioSeleccionado = signal<Ejercicio | null>(null);
  cargando = signal<boolean>(false);

  ngOnInit(): void {
    
    this.ejercicioService.getAllEjercicio().subscribe({
      next: (data) => {
        console.log(data.content);
        this.ejercicios.set(data.content);
      },
      error: (err) => {
        console.error('Error al conectar con la base de datos:', err);
      }
    });
  }

  verDetalle(ejercicio: Ejercicio): void {
    this.ejercicioSeleccionado.set(ejercicio);
  }

  cerrarDetalle(): void {
    this.ejercicioSeleccionado.set(null);
  }
}
