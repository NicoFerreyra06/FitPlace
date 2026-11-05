import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { catchError, of, throwError } from 'rxjs';
import { Amigos, PerfilAmigo } from '../../../core/amigos/amigos';
import { Entrenamiento } from '../../../core/entrenamiento/entrenamiento';
import { Records } from '../../../core/records/records';
import { etiquetaRol } from '../../../core/usuario/usuario';
import { Progresion } from '../../components/progresion/progresion';
import { Rachas } from '../../components/rachas/rachas';

/** Respuestas esperables del backend que se muestran como estado de la pantalla, no como falla. */
type SinPerfil = 'no-encontrado' | 'sin-acceso';

@Component({
  imports: [DatePipe, RouterLink, Rachas, Progresion],
  selector: 'app-amigo-detalle',
  styleUrl: './amigo-detalle.css',
  templateUrl: './amigo-detalle.html',
})
export class AmigoDetalle {
  private readonly route = inject(ActivatedRoute);
  private readonly amigosService = inject(Amigos);
  private readonly recordsService = inject(Records);
  private readonly entrenamientoService = inject(Entrenamiento);

  private readonly amigoId = Number(this.route.snapshot.paramMap.get('id'));
  private readonly idValido = Number.isInteger(this.amigoId) && this.amigoId > 0;

  /**
   * 404 (no existe) y 403 (no es tu amigo) se convierten en un estado con mensaje propio.
   * Cualquier otro error queda como error del recurso y se ofrece "Reintentar".
   */
  protected readonly resultado = rxResource({
    params: () => (this.idValido ? { id: this.amigoId } : undefined),
    stream: ({ params }) =>
      this.amigosService.getPerfilAmigo(params.id).pipe(
        catchError((err: HttpErrorResponse) => {
          if (err.status === 404) return of<SinPerfil>('no-encontrado');
          if (err.status === 403) return of<SinPerfil>('sin-acceso');
          return throwError(() => err);
        }),
      ),
  });

  protected readonly amigo = computed<PerfilAmigo | null>(() => {
    if (!this.resultado.hasValue()) return null;
    const valor = this.resultado.value();
    return typeof valor === 'string' ? null : valor;
  });

  protected readonly motivoSinPerfil = computed<SinPerfil | null>(() => {
    if (!this.idValido) return 'no-encontrado';
    if (!this.resultado.hasValue()) return null;
    const valor = this.resultado.value();
    return typeof valor === 'string' ? valor : null;
  });

  /** "80 kg · 1.75 m", solo con los datos que el amigo tenga cargados. */
  protected readonly datosFisicos = computed(() => {
    const a = this.amigo();
    if (!a) return null;
    const partes = [a.peso ? `${a.peso} kg` : null, a.altura ? `${a.altura} m` : null];
    return partes.filter((p) => p !== null).join(' · ') || null;
  });

  /** Se piden recién cuando el perfil confirmó que es amigo, así no se dispara un 403 de más. */
  protected readonly records = rxResource({
    params: () => {
      const a = this.amigo();
      return a ? { id: a.id } : undefined;
    },
    stream: ({ params }) => this.recordsService.getRecordsDeUsuario(params.id),
  });

  /** Los más recientes primero: muestran por dónde viene progresando. */
  protected readonly recordsRecientes = computed(() =>
    this.records.hasValue()
      ? [...this.records.value()].sort((a, b) => b.fechaLogro.localeCompare(a.fechaLogro))
      : [],
  );

  /** Ejercicio con la progresión desplegada. Se abre de a uno para no pedir todas juntas. */
  protected readonly ejercicioAbierto = signal<number | null>(null);

  protected readonly progresion = rxResource({
    params: () => {
      const a = this.amigo();
      const ejercicioId = this.ejercicioAbierto();
      return a && ejercicioId !== null ? { idUsuario: a.id, ejercicioId } : undefined;
    },
    stream: ({ params }) =>
      this.entrenamientoService.getProgresionEjercicio(params.idUsuario, params.ejercicioId),
  });

  protected readonly puntosProgresion = computed(() =>
    this.progresion.hasValue() ? this.progresion.value() : [],
  );

  protected alternarProgresion(ejercicioId: number): void {
    this.ejercicioAbierto.update((abierto) => (abierto === ejercicioId ? null : ejercicioId));
  }

  protected readonly etiquetaRol = etiquetaRol;
}
