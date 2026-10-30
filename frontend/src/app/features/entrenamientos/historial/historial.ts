import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Usuario } from '../../../core/usuario/usuario';
import {
  Entrenamiento,
  EntrenamientoLog,
  MarcaEjercicio,
} from '../../../core/entrenamiento/entrenamiento';

type MarcaForm = FormGroup<{
  ejercicioRutinaId: FormControl<number>;
  pesoLevantado: FormControl<number>;
  repeticionesLogradas: FormControl<number>;
}>;

/** El backend solo permite editar entrenamientos de los últimos 7 días. */
const DIAS_EDITABLES = 7;

@Component({
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  selector: 'app-historial',
  styleUrl: './historial.css',
  templateUrl: './historial.html',
})
export class Historial {
  private readonly usuarioService = inject(Usuario);
  private readonly entrenamientoService = inject(Entrenamiento);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly pagina = signal(0);
  protected readonly desde = signal('');
  protected readonly hasta = signal('');
  protected readonly editandoId = signal<number | null>(null);
  protected readonly borrandoId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);

  protected readonly limiteEdicion = Historial.haceDias(DIAS_EDITABLES);

  private readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly historial = rxResource({
    params: () =>
      this.perfil.hasValue()
        ? {
            idUsuario: this.perfil.value().id,
            pagina: this.pagina(),
            desde: this.desde(),
            hasta: this.hasta(),
          }
        : undefined,
    stream: ({ params }) =>
      this.entrenamientoService.getHistorial(
        params.idUsuario,
        params.pagina,
        params.desde,
        params.hasta,
      ),
  });

  protected readonly formEdicion = new FormGroup({ marcas: new FormArray<MarcaForm>([]) });

  protected get marcas() {
    return this.formEdicion.controls.marcas;
  }

  protected get cargando() {
    return this.perfil.isLoading() || (this.historial.isLoading() && !this.historial.hasValue());
  }

  aplicarFiltro(desde: string, hasta: string) {
    this.desde.set(desde);
    this.hasta.set(hasta);
    this.pagina.set(0);
  }

  cambiarPagina(delta: number) {
    this.pagina.update((p) => p + delta);
  }

  editar(entrenamiento: EntrenamientoLog) {
    this.marcas.clear();
    entrenamiento.marcasEjercicio.forEach((m) => this.marcas.push(this.crearMarca(m)));
    this.borrandoId.set(null);
    this.errorMsg.set(null);
    this.editandoId.set(entrenamiento.id);
  }

  cancelarEdicion() {
    this.editandoId.set(null);
    this.errorMsg.set(null);
  }

  guardar(entrenamiento: EntrenamientoLog) {
    const request = {
      idRutina: entrenamiento.idRutina,
      marcasEjercicio: this.marcas.getRawValue(),
    };
    this.entrenamientoService.actualizar(entrenamiento.id, request).subscribe({
      next: () => {
        this.editandoId.set(null);
        this.errorMsg.set(null);
        this.historial.reload();
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo guardar el entrenamiento'),
    });
  }

  pedirBorrado(id: number) {
    this.editandoId.set(null);
    this.errorMsg.set(null);
    this.borrandoId.set(id);
  }

  confirmarBorrado(id: number) {
    const eraElUltimoDeLaPagina = this.historial.value()?.content.length === 1;
    this.entrenamientoService.eliminar(id).subscribe({
      next: () => {
        this.borrandoId.set(null);
        this.errorMsg.set(null);
        if (eraElUltimoDeLaPagina && this.pagina() > 0) {
          this.cambiarPagina(-1);
        } else {
          this.historial.reload();
        }
      },
      error: (err) => this.errorMsg.set(err.error?.message ?? 'No se pudo borrar el entrenamiento'),
    });
  }

  private crearMarca(marca: MarcaEjercicio): MarcaForm {
    return this.formBuilder.nonNullable.group({
      ejercicioRutinaId: [marca.ejercicioRutinaId],
      pesoLevantado: [marca.pesoLevantado, [Validators.required, Validators.min(0)]],
      repeticionesLogradas: [marca.repeticionesLogradas, [Validators.required, Validators.min(1)]],
    });
  }

  /** Fecha de hace N días en formato "YYYY-MM-DD" (hora local), comparable con `fecha`. */
  private static haceDias(dias: number): string {
    const d = new Date();
    d.setDate(d.getDate() - dias);
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mes}-${dia}`;
  }
}
