import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AlumnoService } from './alumno';

describe('AlumnoService', () => {
  let service: AlumnoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AlumnoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
