import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { EjercicioService } from '../../../../core/services/ejercicio-service';
import { Ejercicio } from '../../../../model/ejercicio';
import { EjercicioDetails } from '../ejercicio-details/ejercicio-details';
import { FormsModule } from '@angular/forms';

@Component({
  imports: [EjercicioDetails, FormsModule],
  selector: 'app-ejercicio-list',
  styleUrl: './ejercicio-list.css',
  templateUrl: './ejercicio-list.html',
})
export class EjercicioList implements OnInit {
  private ejercicioService = inject(EjercicioService);

  ejercicios = signal<Ejercicio[]>([]);
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
