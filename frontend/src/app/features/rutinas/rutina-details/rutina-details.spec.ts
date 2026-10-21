import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { RutinaDetails } from './rutina-details';
import { Rutinas } from '../../../core/rutinas/rutinas';

describe('RutinaDetails', () => {
  let component: RutinaDetails;
  let fixture: ComponentFixture<RutinaDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RutinaDetails],
      providers: [
        provideRouter([]),
        { provide: Rutinas, useValue: { getRutina: () => of({ id: 1, nombre: 'Test', esPublica: false, diaRutinas: [] }) } },
      ],
    })
      .compileComponents();

    fixture = TestBed.createComponent(RutinaDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
