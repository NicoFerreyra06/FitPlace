import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GimnasiosAdmin } from './gimnasios-admin';

describe('GimnasiosAdmin', () => {
  let http: HttpTestingController;

  const gymPage = (name = 'FitPlace Demo') => ({
    content: [{
      id: 7,
      nombre: name,
      direccion: 'Calle Demo 123',
      precioCuota: 12000,
      idAdmin: 3,
      nombreAdmin: 'admin-gym-demo',
      diasDeApertura: ['MONDAY'],
      horarioApertura: '08:00:00',
      horarioCierre: '21:00:00',
      fotoUrl: null,
      isMercadoPagoVinculado: false,
    }],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
    empty: false,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GimnasiosAdmin],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('lists all gyms and lets an admin edit and delete one', async () => {
    const fixture = TestBed.createComponent(GimnasiosAdmin);
    fixture.detectChanges();

    http.expectOne('http://localhost:8080/gimnasios?page=0').flush(gymPage());
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('admin-gym-demo');

    const editButton = fixture.nativeElement.querySelector('tbody .btn--contorno') as HTMLButtonElement;
    editButton.click();
    fixture.detectChanges();

    const nameInput = fixture.nativeElement.querySelector('input[formControlName="nombre"]') as HTMLInputElement;
    nameInput.value = 'FitPlace Actualizado';
    nameInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.editor form button[type="submit"]') as HTMLButtonElement).click();

    const update = http.expectOne('http://localhost:8080/gimnasios/7');
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({
      nombre: 'FitPlace Actualizado',
      direccion: 'Calle Demo 123',
      precioCuota: 12000,
      horarioApertura: '08:00',
      horarioCierre: '21:00',
      diasAbierto: ['MONDAY'],
      fotoUrl: null,
    });
    update.flush({});
    fixture.detectChanges();
    http.expectOne('http://localhost:8080/gimnasios?page=0').flush(gymPage('FitPlace Actualizado'));
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Los datos del gimnasio se actualizaron correctamente.');

    const deleteButton = Array.from(fixture.nativeElement.querySelectorAll('tbody .btn--peligro'))[0] as HTMLButtonElement;
    deleteButton.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('¿Eliminar FitPlace Actualizado?');

    const confirmButton = fixture.nativeElement.querySelector(
      '.confirmacion button.btn--peligro',
    ) as HTMLButtonElement;
    confirmButton.click();

    const deletion = http.expectOne('http://localhost:8080/gimnasios/7');
    expect(deletion.request.method).toBe('DELETE');
    deletion.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    http.expectOne('http://localhost:8080/gimnasios?page=0').flush({
      ...gymPage(),
      content: [],
      totalElements: 0,
      empty: true,
    });
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('El gimnasio se eliminó correctamente.');
    fixture.destroy();
  });
});
