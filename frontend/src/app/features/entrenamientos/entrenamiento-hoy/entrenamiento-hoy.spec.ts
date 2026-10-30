import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EntrenamientoHoy } from './entrenamiento-hoy';

describe('EntrenamientoHoy', () => {
  let component: EntrenamientoHoy;
  let fixture: ComponentFixture<EntrenamientoHoy>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EntrenamientoHoy],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(EntrenamientoHoy);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
