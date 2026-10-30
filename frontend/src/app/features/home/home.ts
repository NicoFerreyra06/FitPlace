import { Component, computed, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { Usuario } from '../../core/usuario/usuario';
import { Rutinas } from '../../core/rutinas/rutinas';
import { DIAS_SEMANA } from '../../core/rutinas/rutinas.model';
import { Entrenamiento } from '../../core/entrenamiento/entrenamiento';
import { Records } from '../../core/records/records';
import { RouterLink } from '@angular/router';
import { formatDate } from '@angular/common';

@Component({
  imports: [RouterLink],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {
  private readonly usuarioService = inject(Usuario);
  private readonly rutinaService = inject(Rutinas);
  private readonly entrenamientoService = inject(Entrenamiento);
  private readonly recordsService = inject(Records);

  private readonly hoy = new Date();

  private readonly diaDeHoy = DIAS_SEMANA[(this.hoy.getDay() + 6) % 7].valor;
  private readonly inicioDeMes = formatDate(this.hoy, 'yyyy-MM-01', 'en');

  private readonly titularesSinRutina = ['Elegí una rutina', 'Armá tu plan', 'Empezá hoy', 'Tu primera rutina te espera'];
  protected readonly titularSinRutina = this.titularesSinRutina[Math.floor(Math.random() * this.titularesSinRutina.length)];

  private readonly titularesDescanso = ['Hoy descansás', 'Día libre', 'Hoy toca recuperar', 'Descanso también es entrenar'];
  protected readonly titularDescanso = this.titularesDescanso[Math.floor(Math.random() * this.titularesDescanso.length)];

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly rutina = rxResource({
    params: () => {
      const idRutina = this.perfil.hasValue() ? this.perfil.value().rutinaActivaId : null;
      return idRutina ? { idRutina } : undefined;
    },
    stream: ({ params }) => this.rutinaService.getRutina(params.idRutina),
  });

  protected readonly entrenosMes = rxResource({
    params: () => (this.perfil.hasValue() ? { idUsuario: this.perfil.value().id } : undefined),
    stream: ({ params }) =>
      this.entrenamientoService.getHistorial(params.idUsuario, 0, this.inicioDeMes),
  });

  protected readonly ultimoRecord = rxResource({
    stream: () => this.recordsService.getUltimoRecord(),
  });

  protected readonly estado = computed(() => {
    if (this.perfil.error() || this.rutina.error()) {
      return 'error';
    }
    if (!this.perfil.hasValue()) {
      return 'cargando';
    }
    if (!this.perfil.value().rutinaActivaId) {
      return 'sinRutina';
    }
    if (!this.rutina.hasValue()) {
      return 'cargando';
    }
    return this.ejerciciosDeHoy().length ? 'entrenar' : 'descanso';
  });

  protected franjaHoraria() {
    const hora = this.hoy.getHours();

    if (hora < 6){
      return 'madrugada';
    } else if (hora < 12) {
      return 'mañana';
    } else if (hora < 20) {
      return 'tarde';
    } else {
      return 'noche';
    }
  }

  protected readonly nombreRutina = computed(() =>
    this.rutina.hasValue() ? this.rutina.value().nombre : null,
  );

  protected readonly ejerciciosDeHoy = computed(() =>
    this.rutina.hasValue()
      ? (this.rutina.value().diaRutinas.find((d) => d.diaDeLaSemana === this.diaDeHoy)
          ?.ejercicioRutinas ?? [])
      : [],
  );

  protected readonly primerEjercicio = computed(() => this.ejerciciosDeHoy()[0]?.nombreEjercicio);

  protected readonly racha = computed(() =>
    this.perfil.hasValue() ? (this.perfil.value().rachaActualDias ?? 0) : 0,
  );

  protected readonly sinRecords = computed(
    () => (this.ultimoRecord.error() as HttpErrorResponse | undefined)?.status === 404,
  );
}
