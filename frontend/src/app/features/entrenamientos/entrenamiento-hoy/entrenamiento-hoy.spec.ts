import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EntrenamientoHoy } from './entrenamiento-hoy';
import { Usuario } from '../../../core/usuario/usuario';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Entrenamiento } from '../../../core/entrenamiento/entrenamiento';

describe('EntrenamientoHoy', () => {
  let component: EntrenamientoHoy;
  let fixture: ComponentFixture<EntrenamientoHoy>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EntrenamientoHoy],
      providers: [
        provideRouter([]),
        { provide: Usuario, useValue: { getMiPerfil: () => of({ rutinaActivaId: 1 }) } },
        { provide: Rutinas, useValue: { getDiaDeHoy: () => of({ id: 1, diaDeLaSemana: 'MONDAY', ejercicioRutinas: [] }) } },
        { provide: Entrenamiento, useValue: { registrar: () => of({}) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EntrenamientoHoy);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
