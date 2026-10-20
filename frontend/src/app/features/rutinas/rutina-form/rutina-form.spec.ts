import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { RutinaForm } from './rutina-form';
import { Ejercicios } from '../../../core/ejercicios/ejercicios';
import { Rutinas } from '../../../core/rutinas/rutinas';

describe('RutinaForm', () => {
  let component: RutinaForm;
  let fixture: ComponentFixture<RutinaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RutinaForm],
      providers: [
        provideRouter([]),
        { provide: Ejercicios, useValue: { getTodos: () => of({ content: [] }) } },
        { provide: Rutinas, useValue: { getRutina: () => of(null) } },
      ],
    })
      .compileComponents();

    fixture = TestBed.createComponent(RutinaForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
