import { TestBed } from '@angular/core/testing';
import { EntrenadorService } from './entrenador';

describe('Entrenador', () => {
  let service: EntrenadorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EntrenadorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
