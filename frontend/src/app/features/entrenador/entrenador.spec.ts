import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Entrenador } from './entrenador';

describe('Entrenador', () => {
  let component: Entrenador;
  let fixture: ComponentFixture<Entrenador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Entrenador]
    })
      .compileComponents();

    fixture = TestBed.createComponent(Entrenador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
