import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { catchError, Observable, of, throwError } from 'rxjs';
import { Usuario } from '../../../core/usuario/usuario';
import { Records, RecordPersonalResponse } from '../../../core/records/records';
import { EjercicioService } from '../../../core/services/ejercicio-service';
import { Entrenamiento } from '../../../core/entrenamiento/entrenamiento';
import { Page } from '../../../model/page';
import { Progresion } from '../../components/progresion/progresion';

/** Convierte las respuestas "no existe" del backend en null para mostrarlas como estado vacío y no como error. */
function nullSiNoExiste<T>(estados: number[]) {
  return (fuente: Observable<T>): Observable<T | null> =>
    fuente.pipe(
      catchError((err: HttpErrorResponse) =>
        estados.includes(err.status) ? of(null) : throwError(() => err),
      ),
    );
}

@Component({
  imports: [DatePipe, RouterLink, Progresion],
  selector: 'app-record-detalle',
  styleUrl: './record-detalle.css',
  templateUrl: './record-detalle.html',
})
export class RecordDetalle {
  private readonly route = inject(ActivatedRoute);
  private readonly usuarioService = inject(Usuario);
  private readonly recordsService = inject(Records);
  private readonly ejercicioService = inject(EjercicioService);
  private readonly entrenamientoService = inject(Entrenamiento);

  protected readonly ejercicioId = Number(this.route.snapshot.paramMap.get('ejercicioId'));

  /** Última página del ranking pedida; "Ver más" la avanza de a una. */
  protected readonly pagina = signal(0);

  /** Un id inexistente (404) o inválido (400) se muestra como "ejercicio no encontrado". */
  protected readonly ejercicio = rxResource({
    stream: () =>
      this.ejercicioService.getEjercicioById(this.ejercicioId).pipe(nullSiNoExiste([400, 404])),
  });

  private readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  /** Peso máximo de cada entrenamiento en este ejercicio. Necesita el id del usuario logueado. */
  protected readonly progresion = rxResource({
    params: () => (this.perfil.hasValue() ? { idUsuario: this.perfil.value().id } : undefined),
    stream: ({ params }) =>
      this.entrenamientoService.getProgresionEjercicio(params.idUsuario, this.ejercicioId),
  });

  /** Mientras el perfil carga, la progresión todavía no se pidió: también cuenta como "cargando". */
  protected readonly progresionCargando = computed(
    () => this.perfil.isLoading() || this.progresion.isLoading(),
  );

  protected readonly progresionConError = computed(() =>
    Boolean(this.perfil.error() || this.progresion.error()),
  );

  protected readonly puntosProgresion = computed(() =>
    this.progresion.hasValue() ? this.progresion.value() : [],
  );

  protected reintentarProgresion(): void {
    if (this.perfil.error()) {
      this.perfil.reload();
    } else {
      this.progresion.reload();
    }
  }

  /** Sin récord en el ejercicio el backend responde 404: es un estado válido, no un error. */
  protected readonly miRecord = rxResource({
    stream: () => this.recordsService.getMiRecord(this.ejercicioId).pipe(nullSiNoExiste([404])),
  });

  protected readonly paginaRanking = rxResource({
    params: () => ({ pagina: this.pagina() }),
    stream: ({ params }) => this.recordsService.getRanking(this.ejercicioId, params.pagina),
  });

  /**
   * Filas acumuladas del ranking: la página 0 arma la lista y cada "Ver más" suma la siguiente al final.
   * Mientras una página carga (o falla) se conservan las filas que ya estaban.
   */
  protected readonly filasRanking = linkedSignal<
    Page<RecordPersonalResponse> | undefined,
    RecordPersonalResponse[]
  >({
    source: () => (this.paginaRanking.hasValue() ? this.paginaRanking.value() : undefined),
    computation: (pagina, previo) => {
      const acumuladas = previo?.value ?? [];
      if (!pagina) return acumuladas;
      return pagina.number === 0 ? pagina.content : [...acumuladas, ...pagina.content];
    },
  });

  /** Total de usuarios en el ranking; conserva el último valor conocido mientras carga otra página. */
  protected readonly totalRanking = linkedSignal<number | undefined, number>({
    source: () =>
      this.paginaRanking.hasValue() ? this.paginaRanking.value().totalElements : undefined,
    computation: (total, previo) => total ?? previo?.value ?? 0,
  });

  protected readonly miUsername = computed(() =>
    this.perfil.hasValue() ? this.perfil.value().username : null,
  );

  /** Posición del usuario dentro de las filas ya cargadas (0 si no aparece todavía). */
  protected readonly miPosicion = computed(() => {
    const username = this.miUsername();
    return username ? this.filasRanking().findIndex((f) => f.nombreUsuario === username) + 1 : 0;
  });

  protected readonly hayMas = computed(
    () => this.paginaRanking.hasValue() && !this.paginaRanking.value().last,
  );

  protected readonly cargandoMas = computed(
    () => this.paginaRanking.isLoading() && this.filasRanking().length > 0,
  );

  protected readonly errorAlCargarMas = computed(
    () => Boolean(this.paginaRanking.error()) && this.filasRanking().length > 0,
  );

  protected verMas(): void {
    this.pagina.update((p) => p + 1);
  }

  protected esPropia(fila: RecordPersonalResponse): boolean {
    return fila.nombreUsuario === this.miUsername();
  }
}
