import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Records } from './records';

describe('Records', () => {
  let service: Records;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Records);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('pide el récord propio de un ejercicio', () => {
    service.getMiRecord(3).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/records/ejercicio/3');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('pide el ranking paginado con 10 filas por defecto', () => {
    service.getRanking(3).subscribe();

    const req = httpMock.expectOne((r) => r.url === 'http://localhost:8080/records/ranking/3');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ content: [] });
  });

  it('pide la página que se le indica del ranking', () => {
    service.getRanking(3, 2).subscribe();

    const req = httpMock.expectOne((r) => r.url === 'http://localhost:8080/records/ranking/3');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [] });
  });
});
