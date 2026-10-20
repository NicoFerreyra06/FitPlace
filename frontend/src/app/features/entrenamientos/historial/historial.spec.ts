import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Historial } from './historial';

describe('Historial', () => {
  let fixture: ComponentFixture<Historial>;
  let httpMock: HttpTestingController;

  const paginaCon = (content: unknown[]) => ({
    content,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
    number: 0,
    size: 10,
    first: true,
    last: true,
    empty: content.length === 0,
  });

  /** Responde el GET /usuarios/me y deja pendiente el pedido del historial. */
  async function cargarPerfil() {
    fixture.detectChanges();
    TestBed.tick();
    httpMock.expectOne('http://localhost:8080/usuarios/me').flush({ id: 5 });
    // el resource actualiza su valor de forma asíncrona: espero un ciclo y disparo los efectos
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();
  }

  function responderHistorial(content: unknown[]) {
    httpMock
      .expectOne((r) => r.url === 'http://localhost:8080/entrenamientos/usuario/5')
      .flush(paginaCon(content));
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Historial],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Historial);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('muestra el estado vacío si no hay entrenamientos', async () => {
    await cargarPerfil();
    responderHistorial([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Todavía no registraste entrenamientos');
  });

  it('lista los entrenamientos con sus marcas', async () => {
    await cargarPerfil();
    responderHistorial([
      {
        id: 1,
        idRutina: 3,
        nombreUsuario: 'valen',
        nombreRutina: 'Push Day',
        fecha: '2026-10-19',
        marcasEjercicio: [
          {
            id: 10,
            ejercicioRutinaId: 12,
            nombreEjercicio: 'Press banca',
            pesoLevantado: 80,
            repeticionesLogradas: 10,
          },
        ],
      },
    ]);
    await fixture.whenStable();

    const texto: string = fixture.nativeElement.textContent;
    expect(texto).toContain('Push Day');
    expect(texto).toContain('Press banca');
    expect(texto).toContain('80 kg');
  });
});
