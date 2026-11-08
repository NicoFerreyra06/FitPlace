import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlumnoDetalle } from './alumno-detalle';

describe('AlumnoDetalle', () => {
  let component: AlumnoDetalle;
  let fixture: ComponentFixture<AlumnoDetalle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlumnoDetalle]
    })
      .compileComponents();

    fixture = TestBed.createComponent(AlumnoDetalle);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
