import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Usuario } from './usuario';

describe('Usuario', () => {
  let service: Usuario;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Usuario);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a page of users for administration', () => {
    service.getUsuarios(2).subscribe();

    const request = http.expectOne('http://localhost:8080/usuarios?page=2');
    expect(request.request.method).toBe('GET');
    request.flush({ content: [], totalPages: 0 });
  });

  it('should fetch a user detail for administration', () => {
    service.getUsuario(7).subscribe();

    const request = http.expectOne('http://localhost:8080/usuarios/7');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, username: 'demo', email: 'demo@test.com', rol: 'USUARIO' });
  });

  it('should send an allowed role change', () => {
    service.cambiarRol(7, 'ENTRENADOR').subscribe();

    const request = http.expectOne('http://localhost:8080/usuarios/7/rol?nuevoRol=ENTRENADOR');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toBeNull();
    request.flush({ id: 7, username: 'demo', email: 'demo@test.com', rol: 'ENTRENADOR' });
  });
});
