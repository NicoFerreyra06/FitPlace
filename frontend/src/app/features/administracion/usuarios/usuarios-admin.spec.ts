import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UsuariosAdmin } from './usuarios-admin';

describe('UsuariosAdmin', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UsuariosAdmin],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('lists users, loads a basic detail and saves an allowed role', async () => {
    const fixture = TestBed.createComponent(UsuariosAdmin);
    fixture.detectChanges();

    http.expectOne('http://localhost:8080/usuarios?page=0').flush({
      content: [{ id: 7, username: 'demo', email: 'demo@test.com', rol: 'USUARIO' }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
      empty: false,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    const detailButton = fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement;
    detailButton.click();
    fixture.detectChanges();

    http.expectOne('http://localhost:8080/usuarios/7').flush({
      id: 7,
      username: 'demo',
      email: 'demo@test.com',
      rol: 'USUARIO',
    });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('demo@test.com');
    const roleSelect = fixture.nativeElement.querySelector('#rol-usuario') as HTMLSelectElement;
    expect(Array.from(roleSelect.options).map((option) => option.value)).toEqual([
      'USUARIO',
      'ENTRENADOR',
      'ADMIN_GIMNASIO',
    ]);

    roleSelect.value = 'ENTRENADOR';
    roleSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    const saveButton = fixture.nativeElement.querySelector('.formulario-rol button') as HTMLButtonElement;
    saveButton.click();
    fixture.detectChanges();

    http.expectOne('http://localhost:8080/usuarios/7/rol?nuevoRol=ENTRENADOR').flush({
      id: 7,
      username: 'demo',
      email: 'demo@test.com',
      rol: 'ENTRENADOR',
    });
    fixture.detectChanges();
    http.expectOne('http://localhost:8080/usuarios?page=0').flush({
      content: [{ id: 7, username: 'demo', email: 'demo@test.com', rol: 'ENTRENADOR' }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
      empty: false,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('El rol del usuario se actualizó correctamente.');
    fixture.destroy();
  });
});
