import { TestBed } from '@angular/core/testing';
import { Ejercicios } from './ejercicios';

describe('Ejercicios', () => {
  let service: Ejercicios;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Ejercicios);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
