import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Gimnasios, gimnasioResponse } from '../../../core/gimnasios/gimnasios';

const DIAS_SEMANA = [
  { valor: 'MONDAY', etiqueta: 'Lunes' },
  { valor: 'TUESDAY', etiqueta: 'Martes' },
  { valor: 'WEDNESDAY', etiqueta: 'Miércoles' },
  { valor: 'THURSDAY', etiqueta: 'Jueves' },
  { valor: 'FRIDAY', etiqueta: 'Viernes' },
  { valor: 'SATURDAY', etiqueta: 'Sábado' },
  { valor: 'SUNDAY', etiqueta: 'Domingo' },
] as const;

const horarioValido: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const apertura = control.get('horarioApertura')?.value;
  const cierre = control.get('horarioCierre')?.value;

  if (typeof apertura !== 'string' || typeof cierre !== 'string' || !apertura || !cierre) return null;
  return cierre > apertura ? null : { horarioInvalido: true };
};

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-gimnasios-admin',
  templateUrl: './gimnasios-admin.html',
  styleUrl: './gimnasios-admin.css',
})
export class GimnasiosAdmin {
  private readonly gimnasiosService = inject(Gimnasios);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly pagina = signal(0);
  protected readonly gimnasios = rxResource({
    params: () => this.pagina(),
    stream: ({ params }) => this.gimnasiosService.getAllGimnasios(params),
  });
  protected readonly diasSemana = DIAS_SEMANA;
  protected readonly gimnasioEditando = signal<gimnasioResponse | null>(null);
  protected readonly confirmandoEliminacion = signal<number | null>(null);
  protected readonly eliminandoId = signal<number | null>(null);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(3), Validators.maxLength(60)]],
    direccion: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(3), Validators.maxLength(60)]],
    precioCuota: [0, [Validators.required, Validators.min(0.1)]],
    horarioApertura: ['', Validators.required],
    horarioCierre: ['', Validators.required],
    diasAbierto: this.fb.array<string>([], Validators.required),
    fotoUrl: ['', Validators.maxLength(255)],
  }, { validators: horarioValido });

  protected editar(gimnasio: gimnasioResponse): void {
    this.error.set(null);
    this.exito.set(null);
    this.gimnasioEditando.set(gimnasio);
    this.form.patchValue({
      nombre: gimnasio.nombre,
      direccion: gimnasio.direccion,
      precioCuota: gimnasio.precioCuota,
      horarioApertura: gimnasio.horarioApertura.slice(0, 5),
      horarioCierre: gimnasio.horarioCierre.slice(0, 5),
      fotoUrl: gimnasio.fotoUrl ?? '',
    });

    const dias = this.form.controls.diasAbierto;
    dias.clear();
    gimnasio.diasDeApertura.forEach((dia) => {
      const valor = DIAS_SEMANA.find((opcion) => opcion.etiqueta === dia)?.valor ?? dia;
      dias.push(this.fb.control(valor));
    });
    this.form.markAsPristine();
  }

  protected cancelarEdicion(): void {
    this.gimnasioEditando.set(null);
    this.form.reset();
    this.form.controls.diasAbierto.clear();
    this.error.set(null);
  }

  protected cambiarDia(dia: string, event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;

    const dias = this.form.controls.diasAbierto;
    const indice = dias.controls.findIndex((control) => control.value === dia);
    if (event.target.checked && indice === -1) dias.push(this.fb.control(dia));
    else if (!event.target.checked && indice !== -1) dias.removeAt(indice);
    dias.markAsTouched();
  }

  protected guardar(): void {
    const gimnasio = this.gimnasioEditando();
    if (!gimnasio || this.guardando()) return;

    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    const valor = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set(null);
    this.gimnasiosService.actualizarCualquierGimnasio(gimnasio.id, {
      nombre: valor.nombre.trim(),
      direccion: valor.direccion.trim(),
      precioCuota: valor.precioCuota,
      horarioApertura: valor.horarioApertura,
      horarioCierre: valor.horarioCierre,
      diasAbierto: valor.diasAbierto,
      fotoUrl: valor.fotoUrl.trim() || null,
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.gimnasioEditando.set(null);
        this.exito.set('Los datos del gimnasio se actualizaron correctamente.');
        this.gimnasios.reload();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.message ?? 'No se pudieron guardar los cambios.');
      },
    });
  }

  protected eliminar(gimnasio: gimnasioResponse): void {
    if (this.eliminandoId() !== null) return;
    this.eliminandoId.set(gimnasio.id);
    this.error.set(null);
    this.exito.set(null);

    this.gimnasiosService.eliminarGimnasio(gimnasio.id).subscribe({
      next: () => {
        this.eliminandoId.set(null);
        this.confirmandoEliminacion.set(null);
        this.exito.set('El gimnasio se eliminó correctamente.');
        this.gimnasios.reload();
      },
      error: (err) => {
        this.eliminandoId.set(null);
        this.confirmandoEliminacion.set(null);
        this.error.set(err.error?.message ?? 'No se pudo eliminar el gimnasio.');
      },
    });
  }

  protected cambiarPagina(delta: number): void {
    this.pagina.update((pagina) => pagina + delta);
  }
}
