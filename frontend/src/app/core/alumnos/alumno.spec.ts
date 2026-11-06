import { TestBed } from '@angular/core/testing';
import { Alumno } from './alumno';

describe('Alumno', () => {
  let service: Alumno;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Alumno);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
