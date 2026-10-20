import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EjercicioComponent } from './ejercicios';

describe('EjercicioComponent', () => {
  let component: EjercicioComponent;
  let fixture: ComponentFixture<EjercicioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EjercicioComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EjercicioComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
