import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Entrenamiento } from './entrenamiento';

describe('Entrenamiento', () => {
  let service: Entrenamiento;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Entrenamiento);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('pide el historial paginado y agrega los filtros solo si tienen valor', () => {
    service.getHistorial(5, 1, '2026-10-01').subscribe();

    const req = httpMock.expectOne(
      (r) => r.url === 'http://localhost:8080/entrenamientos/usuario/5',
    );
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('desde')).toBe('2026-10-01');
    expect(req.request.params.has('hasta')).toBe(false);
    req.flush({ content: [] });
  });

  it('borra un entrenamiento con DELETE', () => {
    service.eliminar(7).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/entrenamientos/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
