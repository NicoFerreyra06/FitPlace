import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GananciasAdmin } from './ganancias-admin';

describe('GananciasAdmin', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GananciasAdmin],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('validates the date range and shows report amounts', () => {
    const fixture = TestBed.createComponent(GananciasAdmin);
    fixture.detectChanges();

    const [desdeInput, hastaInput] = Array.from(
      fixture.nativeElement.querySelectorAll('input[type="date"]'),
    ) as HTMLInputElement[];
    const submitButton = fixture.nativeElement.querySelector('form button') as HTMLButtonElement;

    desdeInput.value = '2026-02-01';
    desdeInput.dispatchEvent(new Event('input'));
    hastaInput.value = '2026-01-31';
    hastaInput.dispatchEvent(new Event('input'));
    submitButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'La fecha desde no puede ser posterior a la fecha hasta.',
    );
    http.expectNone((request) => request.url.includes('/usuarios/ganancias'));

    desdeInput.value = '2026-01-01';
    desdeInput.dispatchEvent(new Event('input'));
    hastaInput.value = '2026-01-31';
    hastaInput.dispatchEvent(new Event('input'));
    submitButton.click();
    fixture.detectChanges();

    http.expectOne(
      'http://localhost:8080/usuarios/ganancias?desde=2026-01-01&hasta=2026-01-31',
    ).flush([
      {
        nombreGimnasio: 'FitPlace Demo',
        totalIngresosBrutos: 10000,
        comisionFitPlace: 1000,
        gananciaNetaGimnasio: 9000,
      },
    ]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('FitPlace Demo');
    expect(fixture.nativeElement.textContent).toContain('Comisión FitPlace');
    expect(fixture.nativeElement.textContent).toContain('1.000,00');
    fixture.destroy();
  });
});
