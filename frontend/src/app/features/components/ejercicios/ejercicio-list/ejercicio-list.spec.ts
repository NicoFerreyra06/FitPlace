import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EjercicioList } from './ejercicio-list';

describe('EjercicioList', () => {
  let component: EjercicioList;
  let fixture: ComponentFixture<EjercicioList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EjercicioList],
    }).compileComponents();

    fixture = TestBed.createComponent(EjercicioList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
