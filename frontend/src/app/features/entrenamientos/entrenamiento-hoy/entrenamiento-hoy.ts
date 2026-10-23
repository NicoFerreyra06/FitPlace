import { Component, effect, inject, signal } from '@angular/core';
import { Usuario } from '../../../core/usuario/usuario';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { rxResource } from '@angular/core/rxjs-interop';
import {
  FormGroup,
  FormControl,
  FormArray,
  ReactiveFormsModule,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { EjercicioRutinaResponse } from '../../../core/rutinas/rutinas.model';
import { Entrenamiento, EntrenamientoLogRequest } from '../../../core/entrenamiento/entrenamiento';
import { Router } from '@angular/router';

type MarcaForm = FormGroup<{
  ejercicioRutinaId: FormControl<number>;
  repeticionesLogradas: FormControl<number>;
  pesoLevantado: FormControl<number>;
}>;

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-entrenamiento-hoy',
  styleUrl: './entrenamiento-hoy.css',
  templateUrl: './entrenamiento-hoy.html',
})
export class EntrenamientoHoy {
  private readonly usuarioService = inject(Usuario);
  private readonly rutinaService = inject(Rutinas);
  private readonly entrenamientoService = inject(Entrenamiento);

  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly errorMsg = signal<string | null>(null);

  private readonly perfil = rxResource({
    stream: () => this.usuarioService.getMiPerfil(),
  });

  protected readonly diaDeRutina = rxResource({
    stream: () => this.rutinaService.getDiaDeHoy(),
  });

  protected readonly formMarcas = new FormGroup({ marcas: new FormArray<MarcaForm>([]) });

  constructor() {
    effect(() => {
      const dia = this.diaDeRutina.value();

      if (!dia) return;

      this.marcas.clear();

      for (const d of dia.ejercicioRutinas) {
        this.marcas.push(this.crearMarca(d));
      }
    });
  }

  protected get marcas() {
    return this.formMarcas.controls.marcas;
  }

  private crearMarca(ejercicio: EjercicioRutinaResponse): MarcaForm {
    return this.formBuilder.nonNullable.group({
      ejercicioRutinaId: [ejercicio.id],
      repeticionesLogradas: [ejercicio.repeticiones, [Validators.required, Validators.min(1)]],
      pesoLevantado: [0, [Validators.required, Validators.min(0)]],
    });
  }

  guardar() {
    const idRutina = this.perfil.value()?.rutinaActivaId;
    const marcasEjercicio = this.marcas.getRawValue();

    if (idRutina) {
      const entrenamientoLogRequest: EntrenamientoLogRequest = { idRutina, marcasEjercicio };
      this.entrenamientoService.registrar(entrenamientoLogRequest).subscribe({
        next: () => this.router.navigate(['/entrenamientos']),
        error: (err) =>
          this.errorMsg.set(err.error?.message ?? 'No se pudo guardar el entrenamiento'),
      });
    }
  }
}
