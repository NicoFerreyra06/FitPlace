import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { RecordDetalle } from './record-detalle';

const API = 'http://localhost:8080';

const fila = (id: number, nombreUsuario: string, pesoMaximo: number) => ({
  id,
  ejercicioId: 1,
  nombreUsuario,
  nombreEjercicio: 'Press banca',
  pesoMaximo,
  fechaLogro: '2026-10-20',
});

const pagina = (content: unknown[], number: number, totalElements: number, last: boolean) => ({
  content,
  totalElements,
  totalPages: Math.ceil(totalElements / 10),
  number,
  size: 10,
  first: number === 0,
  last,
  empty: content.length === 0,
});

describe('RecordDetalle', () => {
  let fixture: ComponentFixture<RecordDetalle>;
  let httpMock: HttpTestingController;

  /** El resource actualiza su valor de forma asíncrona: espero un ciclo, disparo efectos y re-renderizo. */
  async function estabilizar() {
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();
    await fixture.whenStable();
  }

  function iniciar() {
    fixture.detectChanges();
    TestBed.tick();
  }

  function responderEjercicioYPerfil() {
    httpMock.expectOne(`${API}/ejercicios/1`).flush({ id: 1, nombre: 'Press banca' });
    httpMock.expectOne(`${API}/usuarios/me`).flush({ id: 5, username: 'valen' });
  }

  function responderMiRecord(record: object | null) {
    const req = httpMock.expectOne(`${API}/records/ejercicio/1`);
    if (record === null) {
      req.flush({ message: 'No tenés record en ese ejercicio' }, { status: 404, statusText: 'Not Found' });
    } else {
      req.flush(record);
    }
  }

  function responderRanking(numeroPagina: number, body: object) {
    const req = httpMock.expectOne((r) => r.url === `${API}/records/ranking/1`);
    expect(req.request.params.get('page')).toBe(String(numeroPagina));
    req.flush(body);
  }

  const texto = () => fixture.nativeElement.textContent as string;
  const filas = () => fixture.nativeElement.querySelectorAll('tbody tr') as NodeListOf<HTMLElement>;
  const botonVerMas = () =>
    Array.from(fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>).find(
      (b) => b.textContent?.includes('Ver más'),
    );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecordDetalle],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ ejercicioId: '1' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecordDetalle);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('muestra el ranking con posiciones y marca la fila del usuario logueado', async () => {
    iniciar();
    responderEjercicioYPerfil();
    responderMiRecord(fila(2, 'valen', 100));
    responderRanking(0, pagina([fila(1, 'nico', 120), fila(2, 'valen', 100)], 0, 2, true));
    await estabilizar();

    expect(texto()).toContain('Press banca');
    expect(filas().length).toBe(2);
    expect(filas()[0].textContent).toContain('1');
    expect(filas()[0].classList.contains('propia')).toBe(false);
    expect(filas()[1].classList.contains('propia')).toBe(true);
    expect(filas()[1].textContent).toContain('Vos');
    expect(texto()).toContain('Puesto #2 de 2');
    expect(botonVerMas()).toBeUndefined();
  });

  it('muestra que todavía no hay récord propio cuando el backend responde 404', async () => {
    iniciar();
    responderEjercicioYPerfil();
    responderMiRecord(null);
    responderRanking(0, pagina([fila(1, 'nico', 120)], 0, 1, true));
    await estabilizar();

    expect(texto()).toContain('Todavía no tenés récord en este ejercicio');
    expect(texto()).not.toContain('No se pudo cargar tu récord');
  });

  it('"Ver más" pide la página siguiente y suma las filas al final', async () => {
    const primeras = Array.from({ length: 10 }, (_, i) => fila(i + 1, `user${i + 1}`, 200 - i));
    iniciar();
    responderEjercicioYPerfil();
    responderMiRecord(null);
    responderRanking(0, pagina(primeras, 0, 12, false));
    await estabilizar();

    expect(filas().length).toBe(10);
    botonVerMas()!.click();
    TestBed.tick();

    responderRanking(1, pagina([fila(11, 'user11', 150), fila(12, 'user12', 140)], 1, 12, true));
    await estabilizar();

    expect(filas().length).toBe(12);
    expect(filas()[10].textContent).toContain('11');
    expect(filas()[10].textContent).toContain('user11');
    expect(filas()[11].textContent).toContain('user12');
    expect(botonVerMas()).toBeUndefined();
  });

  it('muestra el estado vacío si nadie registró el ejercicio', async () => {
    iniciar();
    responderEjercicioYPerfil();
    responderMiRecord(null);
    responderRanking(0, pagina([], 0, 0, true));
    await estabilizar();

    expect(texto()).toContain('Nadie registró este ejercicio todavía');
  });

  it('avisa si el ejercicio no existe', async () => {
    iniciar();
    httpMock
      .expectOne(`${API}/ejercicios/1`)
      .flush({ message: 'Ejercicio no encontrado' }, { status: 404, statusText: 'Not Found' });
    httpMock.expectOne(`${API}/usuarios/me`).flush({ id: 5, username: 'valen' });
    responderMiRecord(null);
    responderRanking(0, pagina([], 0, 0, true));
    await estabilizar();

    expect(texto()).toContain('No encontramos este ejercicio');
  });
});
