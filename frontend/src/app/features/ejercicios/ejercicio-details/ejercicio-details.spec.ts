import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EjercicioDetails } from './ejercicio-details';

describe('EjercicioDetails', () => {
  let component: EjercicioDetails;
  let fixture: ComponentFixture<EjercicioDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EjercicioDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(EjercicioDetails);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('ejercicio', {
      id: 1,
      nombre: 'Press de banca',
      descripcion: 'Ejercicio de empuje',
      musculosPrincipales: [],
      musculosSecundarios: [],
    });
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
