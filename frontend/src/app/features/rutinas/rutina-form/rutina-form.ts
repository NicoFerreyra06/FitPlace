import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { EjercicioResponse, Ejercicios } from '../../../core/ejercicios/ejercicios';
import {
  DIAS_SEMANA,
  DiaSemana,
  EjercicioRutinaRequest,
  RutinaRequest,
  RutinaResponse,
} from '../../../core/rutinas/rutinas.model';
import { Rutinas } from '../../../core/rutinas/rutinas';

function crearEjercicioForm(fb: NonNullableFormBuilder, ej?: Partial<EjercicioRutinaRequest>) {
  return fb.group({
    ejercicioId: [ej?.ejercicioId ?? 0, [Validators.required, Validators.min(1)]],
    series: [ej?.series ?? 3, [Validators.required, Validators.min(1)]],
    repeticiones: [ej?.repeticiones ?? 10, [Validators.required, Validators.min(1)]],
  });
}

function crearDiaForm(
  fb: NonNullableFormBuilder,
  dia: DiaSemana,
  ejercicios: Partial<EjercicioRutinaRequest>[] = [],
) {
  return fb.group({
    diaDeLaSemana: fb.control<DiaSemana>(dia, Validators.required),
    ejercicios: fb.array(
      ejercicios.map((ej) => crearEjercicioForm(fb, ej)),
      Validators.minLength(1),
    ),
  });
}

type DiaForm = ReturnType<typeof crearDiaForm>;

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-rutina-form',
  styleUrl: './rutina-form.css',
  templateUrl: './rutina-form.html',
})
export class RutinaForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly rutinasService = inject(Rutinas);
  private readonly ejerciciosService = inject(Ejercicios);

  // Si la ruta trae :id estamos editando; si no, estamos creando
  protected readonly idRutina = Number(this.route.snapshot.paramMap.get('id')) || null;
  protected readonly esEdicion = this.idRutina !== null;

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly ejercicios = signal<EjercicioResponse[]>([]);

  protected readonly form = this.fb.group({
    nombre: ['', Validators.required],
    esPublica: [false],
    dias: this.fb.array<DiaForm>([], Validators.minLength(1)),
  });

  protected get dias() {
    return this.form.controls.dias;
  }

  ngOnInit() {
    forkJoin({
      ejercicios: this.ejerciciosService.getTodos(),
      rutina: this.idRutina !== null ? this.rutinasService.getRutina(this.idRutina) : of(null),
    }).subscribe({
      next: ({ ejercicios, rutina }) => {
        this.ejercicios.set(ejercicios.content);
        if (rutina) {
          this.cargarRutina(rutina);
        }
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor');
        this.cargando.set(false);
      },
    });
  }

  private cargarRutina(rutina: RutinaResponse) {
    this.form.patchValue({ nombre: rutina.nombre, esPublica: rutina.esPublica });

    const orden = DIAS_SEMANA.map((d) => d.valor);
    [...rutina.diaRutinas]
      .sort((a, b) => orden.indexOf(a.diaDeLaSemana) - orden.indexOf(b.diaDeLaSemana))
      .forEach((dia) =>
        this.dias.push(
          crearDiaForm(
            this.fb,
            dia.diaDeLaSemana,
            dia.ejercicioRutinas.map((e) => ({
              ejercicioId: e.ejercicioId,
              series: e.series,
              repeticiones: e.repeticiones,
            })),
          ),
        ),
      );
  }

  // ===== Días =====

  protected readonly hayDiasLibres = () => this.dias.length < DIAS_SEMANA.length;

  // Días que todavía no están usados en otra tarjeta (más el del propio select)
  protected diasLibres(indice: number) {
    const usados = this.dias.controls
      .filter((_, k) => k !== indice)
      .map((d) => d.controls.diaDeLaSemana.value);
    return DIAS_SEMANA.filter((d) => !usados.includes(d.valor));
  }

  protected agregarDia() {
    const usados = this.dias.controls.map((d) => d.controls.diaDeLaSemana.value);
    const libre = DIAS_SEMANA.find((d) => !usados.includes(d.valor));
    if (!libre) {
      return;
    }
    this.dias.push(crearDiaForm(this.fb, libre.valor, [{}]));
  }

  protected quitarDia(indice: number) {
    this.dias.removeAt(indice);
  }

  // ===== Ejercicios =====

  // Ejercicios que todavía no están elegidos en otra fila del mismo día
  protected ejerciciosLibres(dia: DiaForm, indice: number) {
    const elegidos = dia.controls.ejercicios.controls
      .filter((_, k) => k !== indice)
      .map((e) => e.controls.ejercicioId.value);
    return this.ejercicios().filter((e) => !elegidos.includes(e.id));
  }

  protected agregarEjercicio(dia: DiaForm) {
    dia.controls.ejercicios.push(crearEjercicioForm(this.fb));
  }

  protected quitarEjercicio(dia: DiaForm, indice: number) {
    dia.controls.ejercicios.removeAt(indice);
  }

  // ===== Guardar =====

  protected guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.errorMsg.set(null);

    const request: RutinaRequest = this.form.getRawValue();
    const peticion =
      this.idRutina !== null
        ? this.rutinasService.actualizar(this.idRutina, request)
        : this.rutinasService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/rutinas']),
      error: (err) => {
        this.errorMsg.set(err.error?.message ?? 'No se pudo guardar la rutina');
        this.guardando.set(false);
      },
    });
  }
}
