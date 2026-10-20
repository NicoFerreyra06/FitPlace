import { TestBed } from '@angular/core/testing';
import { Rutinas } from './rutinas';

describe('Rutinas', () => {
  let service: Rutinas;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Rutinas);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
