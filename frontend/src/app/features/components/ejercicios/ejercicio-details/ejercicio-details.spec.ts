import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EjercicioDetails } from './ejercicio-details';
import { Ejercicio } from '../../../../model/ejercicio';

describe('EjercicioDetails', () => {
  let component: EjercicioDetails;
  let fixture: ComponentFixture<EjercicioDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EjercicioDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(EjercicioDetails);
    const ejercicio: Ejercicio = {
      id: 1,
      nombre: 'Sentadilla',
      descripcion: 'Flexioná las rodillas manteniendo la espalda recta.',
      musculosPrincipales: [],
      musculosSecundarios: [],
    };
    fixture.componentRef.setInput('ejercicio', ejercicio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
