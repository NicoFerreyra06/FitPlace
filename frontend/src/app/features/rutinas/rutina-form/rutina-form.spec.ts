import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { RutinaForm } from './rutina-form';
import { EjercicioService } from '../../../core/services/ejercicio-service';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { RutinaRequest, RutinaResponse } from '../../../core/rutinas/rutinas.model';

const ejercicios = {
  content: [
    { id: 1, nombre: 'Press banca', descripcion: '', musculosPrincipales: [], musculosSecundarios: [] },
  ],
};

describe('RutinaForm', () => {
  let component: RutinaForm;
  let fixture: ComponentFixture<RutinaForm>;
  let enviado: RutinaRequest | undefined;

  async function crearComponente(idRutina: string | null, rutina: RutinaResponse | null = null) {
    enviado = undefined;
    await TestBed.configureTestingModule({
      imports: [RutinaForm],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(idRutina ? { id: idRutina } : {}) } },
        },
        { provide: EjercicioService, useValue: { getAllEjercicio: () => of(ejercicios) } },
        {
          provide: Rutinas,
          useValue: {
            getRutina: () => of(rutina),
            // EMPTY: no emite, así el componente no intenta navegar después de guardar
            crear: (request: RutinaRequest) => {
              enviado = request;
              return EMPTY;
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RutinaForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('should create', async () => {
    await crearComponente(null);
    expect(component).toBeTruthy();
  });

  it('manda el nombre del día recortado y null cuando el día no tiene nombre', async () => {
    await crearComponente(null);

    component['form'].controls.nombre.setValue('Fuerza 4 días');
    component['agregarDia']();
    component['agregarDia']();
    const [lunes, martes] = component['dias'].controls;
    lunes.controls.nombre.setValue('  Push A  ');
    lunes.controls.ejercicios.at(0).controls.ejercicioId.setValue(1);
    martes.controls.nombre.setValue('   ');
    martes.controls.ejercicios.at(0).controls.ejercicioId.setValue(1);

    component['guardar']();

    expect(enviado?.dias.map((d) => d.nombre)).toEqual(['Push A', null]);
  });

  it('no deja guardar un nombre de día de más de 40 caracteres', async () => {
    await crearComponente(null);

    component['form'].controls.nombre.setValue('Fuerza 4 días');
    component['agregarDia']();
    const [lunes] = component['dias'].controls;
    lunes.controls.ejercicios.at(0).controls.ejercicioId.setValue(1);
    lunes.controls.nombre.setValue('a'.repeat(41));

    component['guardar']();

    expect(lunes.controls.nombre.hasError('maxlength')).toBe(true);
    expect(enviado).toBeUndefined();
  });

  it('carga el nombre de cada día al editar una rutina', async () => {
    await crearComponente('7', {
      id: 7,
      creadorId: 1,
      nombre: 'Fuerza 4 días',
      tokenCompartir: 'token',
      esPublica: false,
      diaRutinas: [
        {
          id: 70,
          nombreRutina: 'Fuerza 4 días',
          diaDeLaSemana: 'MONDAY',
          nombre: 'Pull B',
          ejercicioRutinas: [
            { id: 700, ejercicioId: 1, nombreEjercicio: 'Press banca', series: 4, repeticiones: 8 },
          ],
        },
        {
          id: 71,
          nombreRutina: 'Fuerza 4 días',
          diaDeLaSemana: 'WEDNESDAY',
          nombre: null,
          ejercicioRutinas: [
            { id: 710, ejercicioId: 1, nombreEjercicio: 'Press banca', series: 3, repeticiones: 10 },
          ],
        },
      ],
    });

    const nombres = component['dias'].controls.map((d) => d.controls.nombre.value);
    expect(nombres).toEqual(['Pull B', '']);
  });
});
