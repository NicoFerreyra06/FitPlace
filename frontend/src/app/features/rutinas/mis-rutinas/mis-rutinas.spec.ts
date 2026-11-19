import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { MisRutinas } from './mis-rutinas';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';
import { EntrenadorService } from '../../../core/entrenador/entrenador';

describe('MisRutinas', () => {
  let component: MisRutinas;
  let fixture: ComponentFixture<MisRutinas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisRutinas],
      providers: [
        provideRouter([]),
        {
          provide: Rutinas,
          useValue: {
            getRutinasDisponibles: () => of({ content: [], last: true }),
          },
        },
        { provide: Usuario, useValue: { getPerfilGuardado: () => of({ rutinaActivaId: null }) } },
        { provide: EntrenadorService, useValue: { getMiEntrenador: () => of(null) } },
      ],
    })
      .compileComponents();

    fixture = TestBed.createComponent(MisRutinas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
