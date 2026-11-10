import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AbstractControl, FormsModule, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Gimnasios } from '../../../core/gimnasios/gimnasios';
import { estadoSuscripcion, SuscripcionGimnasioResponse, Suscripciones } from '../../../core/suscripciones/suscripciones';

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

  if (typeof apertura !== 'string' || typeof cierre !== 'string' || !apertura || !cierre) {
    return null;
  }

  return cierre > apertura ? null : { horarioInvalido: true };
};

@Component({
  imports: [DatePipe, FormsModule, ReactiveFormsModule, RouterLink],
  selector: 'app-gestion-gimnasio',
  styleUrls: ['./gestion-gimnasio.css', './gestion-gimnasio-form.css'],
  templateUrl: './gestion-gimnasio.html',
})
export class GestionGimnasio {
  private readonly gimnasiosService = inject(Gimnasios);
  private readonly suscripcionService = inject(Suscripciones);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly idGimnasio = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly estados: estadoSuscripcion[] = ['PENDIENTE', 'ACTIVA', 'VENCIDA', 'CANCELADA'];
  protected readonly diasSemana = DIAS_SEMANA;
  
  protected readonly estado = signal<estadoSuscripcion | null>(null);
  protected readonly activandoId = signal<number | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly error = signal(false);
  protected readonly editando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly confirmandoEliminacion = signal(false);
  protected readonly eliminando = signal(false);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(3), Validators.maxLength(60)]],
    direccion: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(3), Validators.maxLength(60)]],
    precioCuota: [0, [Validators.required, Validators.min(0.1)]],
    horarioApertura: ['', Validators.required],
    horarioCierre: ['', Validators.required],
    diasAbierto: this.fb.array<string>([], Validators.required),
    fotoUrl: ['', Validators.maxLength(255)],
  }, { validators: horarioValido });

  protected readonly gimnasio = rxResource({
    stream: () => this.gimnasiosService.getGimnasio(this.idGimnasio),
  });

  protected readonly pendientes = rxResource({
    stream: () => this.suscripcionService.getSuscripcionesGimnasio(this.idGimnasio, 'PENDIENTE'),
  });

  protected readonly suscripciones = rxResource({
    params: () => this.estado(),
    stream: ({ params }) => this.suscripcionService.getSuscripcionesGimnasio(this.idGimnasio, params),
  });

  protected readonly miembros = rxResource({
    stream: () => this.gimnasiosService.getMiembros(this.idGimnasio),
  });

  protected readonly sinAcceso = computed(() => {
    const error = this.miembros.error() as HttpErrorResponse | undefined;
    if (error?.status !== 403 && error?.status !== 404) return null;
    return error.error?.message ?? 'No tenés acceso a este gimnasio.';
  });

  protected readonly cantidadMiembros = computed(() => this.miembros.value()?.length ?? 0);

  protected readonly precio = computed(() =>
    this.gimnasio.hasValue() ? new Intl.NumberFormat('es-AR').format(this.gimnasio.value().precioCuota) : '',
  );

  protected readonly busqueda = signal<string>('');

  protected readonly miembrosFiltrados = computed(() => {
    const lista = this.miembros.value() ?? [];
    const termino = this.busqueda().trim().toLowerCase();

    if (!termino) {
      return lista;
    }

    return lista.filter((miembro) => miembro.username.toLowerCase().includes(termino));
  });

  protected iniciarEdicion(): void {
    const gimnasio = this.gimnasio.value();
    if (!gimnasio) return;

    this.form.patchValue({
      nombre: gimnasio.nombre,
      direccion: gimnasio.direccion,
      precioCuota: gimnasio.precioCuota,
      horarioApertura: gimnasio.horarioApertura.slice(0, 5),
      horarioCierre: gimnasio.horarioCierre.slice(0, 5),
      fotoUrl: gimnasio.fotoUrl ?? '',
    });
    this.form.controls.diasAbierto.clear();
    gimnasio.diasDeApertura.forEach((dia) => this.form.controls.diasAbierto.push(this.fb.control(dia)));
    this.form.markAsPristine();
    this.aviso.set(null);
    this.editando.set(true);
  }

  protected cancelarEdicion(): void {
    this.editando.set(false);
    this.form.reset();
  }

  protected cambiarDia(dia: string, event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;

    const dias = this.form.controls.diasAbierto;
    const indice = dias.controls.findIndex((control) => control.value === dia);

    if (event.target.checked && indice === -1) {
      dias.push(this.fb.control(dia));
    } else if (!event.target.checked && indice !== -1) {
      dias.removeAt(indice);
    }

    dias.markAsTouched();
  }

  protected guardarEdicion(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.guardando.set(true);
    this.aviso.set(null);
    const valor = this.form.getRawValue();

    this.gimnasiosService.actualizarGimnasio(this.idGimnasio, {
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
        this.editando.set(false);
        this.error.set(false);
        this.aviso.set('Los datos del gimnasio se actualizaron correctamente.');
        this.gimnasio.reload();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(true);
        this.aviso.set(err.error?.message ?? 'No se pudieron guardar los cambios.');
      },
    });
  }

  protected eliminarGimnasio(): void {
    this.eliminando.set(true);
    this.aviso.set(null);

    this.gimnasiosService.eliminarGimnasio(this.idGimnasio).subscribe({
      next: () => this.router.navigate(['/gimnasios']),
      error: (err) => {
        this.eliminando.set(false);
        this.confirmandoEliminacion.set(false);
        this.error.set(true);
        this.aviso.set(err.error?.message ?? 'No se pudo eliminar el gimnasio.');
      },
    });
  }

  protected activar(suscripcion: SuscripcionGimnasioResponse, metodoPago: 'EFECTIVO' | 'TRANSFERENCIA'): void {
    this.activandoId.set(suscripcion.id);
    this.aviso.set(null);

    this.suscripcionService.activarSuscripcion(suscripcion.id, metodoPago).subscribe({
      next: () => {
        this.activandoId.set(null);
        this.error.set(false);
        this.aviso.set(`Activaste la suscripción de ${suscripcion.nombreUsuario}.`);
        this.pendientes.reload();
        this.suscripciones.reload();
        this.miembros.reload();
      },
      error: (err) => {
        this.activandoId.set(null);
        this.error.set(true);
        this.aviso.set(err.error?.message ?? 'No se pudo activar la suscripción.');
      },
    });
  }
}
