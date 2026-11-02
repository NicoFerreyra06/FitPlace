import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Home } from './home';
import { Usuario } from '../../core/usuario/usuario';
import { Rutinas } from '../../core/rutinas/rutinas';
import { Entrenamiento } from '../../core/entrenamiento/entrenamiento';
import { Records } from '../../core/records/records';
import { DIAS_SEMANA } from '../../core/rutinas/rutinas.model';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

describe('Home: titular del día', () => {
  /** Rutina con los 7 días cargados, así el test no depende del día en que se corre. */
  function rutinaConNombreDeDia(nombre: string | null) {
    return {
      id: 3,
      creadorId: 1,
      nombre: 'Fuerza 4 días',
      tokenCompartir: 'token',
      esPublica: false,
      diaRutinas: DIAS_SEMANA.map((d, i) => ({
        id: i + 1,
        nombreRutina: 'Fuerza 4 días',
        diaDeLaSemana: d.valor,
        nombre,
        ejercicioRutinas: [
          { id: 10, ejercicioId: 1, nombreEjercicio: 'Press banca', series: 4, repeticiones: 8 },
          { id: 11, ejercicioId: 2, nombreEjercicio: 'Press militar', series: 3, repeticiones: 10 },
        ],
      })),
    };
  }

  async function renderizarCon(nombreDia: string | null): Promise<string> {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        provideRouter([]),
        {
          provide: Usuario,
          useValue: {
            getPerfilGuardado: () =>
              of({ id: 1, username: 'valen', rol: 'USUARIO', rutinaActivaId: 3, rachaActualDias: 2 }),
          },
        },
        { provide: Rutinas, useValue: { getRutina: () => of(rutinaConNombreDeDia(nombreDia)) } },
        { provide: Entrenamiento, useValue: { getHistorial: () => of({ content: [], totalElements: 0 }) } },
        {
          provide: Records,
          useValue: { getUltimoRecord: () => of({ nombreEjercicio: 'Press banca', pesoMaximo: 80 }) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    await fixture.whenStable();
    // el resource de la rutina depende del perfil: espero un ciclo más a que se resuelva
    await new Promise((resolve) => setTimeout(resolve));
    await fixture.whenStable();
    return fixture.nativeElement.textContent as string;
  }

  it('usa el nombre del día y aclara de qué rutina es', async () => {
    const texto = await renderizarCon('Push A');

    expect(texto).toContain('Hoy toca Push A');
    expect(texto).toContain('Rutina Fuerza 4 días');
  });

  it('en un día creado antes de que el nombre fuera obligatorio muestra el nombre de la rutina', async () => {
    const texto = await renderizarCon(null);

    expect(texto).toContain('Hoy toca Fuerza 4 días');
    expect(texto).not.toContain('Rutina Fuerza 4 días');
  });
});
