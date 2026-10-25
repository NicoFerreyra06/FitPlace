import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { RutinasPublicas } from './rutinas-publicas';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuario } from '../../../core/usuario/usuario';

describe('RutinasPublicas', () => {
  let component: RutinasPublicas;
  let fixture: ComponentFixture<RutinasPublicas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RutinasPublicas],
      providers: [
        provideRouter([]),
        { provide: Rutinas, useValue: { getRutinasPublicas: () => of({ content: [] }) } },
        { provide: Usuario, useValue: { getMiPerfil: () => of({ rutinaActivaId: null }) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RutinasPublicas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
