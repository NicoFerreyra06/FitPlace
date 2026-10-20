import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { MisRutinas } from './mis-rutinas';
import { Rutinas } from '../../../core/rutinas/rutinas';
import { Usuarios } from '../../../core/usuarios/usuarios';

describe('MisRutinas', () => {
  let component: MisRutinas;
  let fixture: ComponentFixture<MisRutinas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisRutinas],
      providers: [
        provideRouter([]),
        { provide: Rutinas, useValue: { getMisRutinas: () => of({ content: [] }) } },
        { provide: Usuarios, useValue: { getMe: () => of({ rutinaActivaId: null }) } },
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
