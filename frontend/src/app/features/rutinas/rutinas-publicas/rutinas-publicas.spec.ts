import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RutinasPublicas } from './rutinas-publicas';

describe('RutinasPublicas', () => {
  let component: RutinasPublicas;
  let fixture: ComponentFixture<RutinasPublicas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RutinasPublicas],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(RutinasPublicas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});