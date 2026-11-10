import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GimnasioUpdateRequest, Gimnasios } from './gimnasios';

describe('Gimnasios', () => {
  let service: Gimnasios;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Gimnasios);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('updates a gym through the owner endpoint', () => {
    const requestBody: GimnasioUpdateRequest = {
      nombre: 'FitPlace Centro',
      direccion: 'Calle 123',
      precioCuota: 12000,
      horarioApertura: '08:00',
      horarioCierre: '21:00',
      diasAbierto: ['MONDAY', 'WEDNESDAY'],
      fotoUrl: null,
    };
    let completed = false;

    service.actualizarGimnasio(7, requestBody).subscribe(() => {
      completed = true;
    });

    const request = httpTesting.expectOne('http://localhost:8080/gimnasios/me/7');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(requestBody);
    request.flush({ id: 7, ...requestBody });
    expect(completed).toBe(true);
  });

  it('updates any gym through the admin endpoint', () => {
    const requestBody: GimnasioUpdateRequest = {
      nombre: 'FitPlace Actualizado',
      direccion: 'Calle 456',
      precioCuota: 15000,
      horarioApertura: '09:00',
      horarioCierre: '22:00',
      diasAbierto: ['TUESDAY', 'THURSDAY'],
      fotoUrl: null,
    };

    service.actualizarCualquierGimnasio(7, requestBody).subscribe();

    const request = httpTesting.expectOne('http://localhost:8080/gimnasios/7');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(requestBody);
    request.flush({ id: 7, ...requestBody });
  });

  it('deletes a gym through the gym endpoint', () => {
    let completed = false;

    service.eliminarGimnasio(7).subscribe(() => {
      completed = true;
    });

    const request = httpTesting.expectOne('http://localhost:8080/gimnasios/7');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(completed).toBe(true);
  });
});
