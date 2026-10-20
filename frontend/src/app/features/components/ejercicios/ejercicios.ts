import { Component, inject, OnInit, signal } from '@angular/core';
import { EjercicioService } from '../../../core/services/ejercicio-service';
import { Ejercicio } from '../../../model/ejercicio';

@Component({
  imports: [],
  selector: 'app-ejercicio-component',
  styleUrl: './ejercicios.css',
  templateUrl: './ejercicios.html',
})
export class EjercicioComponent implements OnInit {
  private ejercicioService = inject(EjercicioService);

  ejercicios = signal<Ejercicio[]>([]);

  ngOnInit(): void {
    this.ejercicioService.getAllEjercicio().subscribe({
      next: (data) => this.ejercicios.set(data),
      error: (err) => console.error('Error al cargar ejercicios.', err)
    });
  }
}
