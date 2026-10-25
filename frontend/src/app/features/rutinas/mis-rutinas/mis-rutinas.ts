import { Component, computed, inject, isDevMode, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';
import { DiaRutinaResponse, DiaSemana, etiquetaDia, RutinaResponse } from '../../../core/rutinas/rutinas.model';

const DIAS_POR_INDICE: DiaSemana[] = [
  'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY',
];
const MODO_DEMO = isDevMode() && new URLSearchParams(window.location.search).get('demo') === '1';

function rutinasDeDemo(): RutinaResponse[] {
  const diaActual = DIAS_POR_INDICE[new Date().getDay()];
  return [
    {
      id: 1,
      creadorId: 1,
      nombre: 'Fuerza e hipertrofia',
      tokenCompartir: '',
      esPublica: false,
      diaRutinas: [{
        id: 101,
        diaDeLaSemana: diaActual,
        ejercicioRutinas: [
          { id: 1001, ejercicioId: 1, nombreEjercicio: 'Press de banca', series: 4, repeticiones: 8 },
          { id: 1002, ejercicioId: 2, nombreEjercicio: 'Remo con barra', series: 4, repeticiones: 10 },
          { id: 1003, ejercicioId: 3, nombreEjercicio: 'Press militar', series: 3, repeticiones: 10 },
          { id: 1004, ejercicioId: 4, nombreEjercicio: 'Sentadilla', series: 4, repeticiones: 8 },
        ],
      }],
    },
    {
      id: 2,
      creadorId: 1,
      nombre: 'Rutina de volumen',
      tokenCompartir: '',
      esPublica: false,
      diaRutinas: [],
    },
    {
      id: 3,
      creadorId: 1,
      nombre: 'Full body express',
      tokenCompartir: '',
      esPublica: true,
      diaRutinas: [],
    },
  ];
}

@Component({
  imports: [RouterLink],
  selector: 'app-mis-rutinas',
  styleUrl: './mis-rutinas.css',
  templateUrl: './mis-rutinas.html',
})
export class MisRutinas implements OnInit {
  private readonly rutinasService = inject(Rutinas);
  private readonly usuarioService = inject(Usuario);

  protected readonly cargando = signal(true);
  protected readonly rutinas = signal<RutinaResponse[]>([]);
  protected readonly rutinaActivaId = signal<number | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly mensaje = signal<string | null>(null);
  protected readonly rutinaActiva = computed(
    () => this.rutinas().find((rutina) => rutina.id === this.rutinaActivaId()) ?? null,
  );
  protected readonly diaDeHoy = computed<DiaRutinaResponse | null>(() => {
    const dia = DIAS_POR_INDICE[new Date().getDay()];
    return this.rutinaActiva()?.diaRutinas.find((item) => item.diaDeLaSemana === dia) ?? null;
  });
  protected readonly etiquetaDiaDeHoy = etiquetaDia(DIAS_POR_INDICE[new Date().getDay()]);

  ngOnInit() {
    if (MODO_DEMO) {
      this.rutinas.set(rutinasDeDemo());
      this.rutinaActivaId.set(1);
      this.cargando.set(false);
      return;
    }

    forkJoin({
      rutinas: this.rutinasService.getMisRutinas(),
      usuario: this.usuarioService.getMiPerfil(),
    }).subscribe({
      next: ({ rutinas, usuario }) => {
        this.rutinas.set(rutinas.content);
        this.rutinaActivaId.set(usuario.rutinaActivaId);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor');
        this.cargando.set(false);
      },
    });
  }

  activar(rutina: RutinaResponse) {
    this.errorMsg.set(null);
    this.mensaje.set(null);
    if (MODO_DEMO) {
      this.rutinaActivaId.set(rutina.id);
      this.mensaje.set(`Rutina "${rutina.nombre}" activada.`);
      return;
    }

    this.usuarioService.activarRutina(rutina.id).subscribe({
      next: (usuario) => {
        this.rutinaActivaId.set(usuario.rutinaActivaId);
        this.mensaje.set(`Rutina "${rutina.nombre}" activada.`);
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo activar la rutina'),
    });
  }

  eliminar(rutina: RutinaResponse) {
    if (!confirm(`¿Seguro que querés eliminar la rutina "${rutina.nombre}"?`)) {
      return;
    }

    this.errorMsg.set(null);
    this.rutinasService.eliminar(rutina.id).subscribe({
      next: () => {
        this.rutinas.update((lista) => lista.filter((r) => r.id !== rutina.id));
        if (this.rutinaActivaId() === rutina.id) {
          this.rutinaActivaId.set(null);
        }
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo eliminar la rutina'),
    });
  }
}
