import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Alumnos } from './alumnos';

describe('Alumnos', () => {
  let component: Alumnos;
  let fixture: ComponentFixture<Alumnos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Alumnos]
    })
      .compileComponents();

    fixture = TestBed.createComponent(Alumnos);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
