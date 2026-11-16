import { aSerieApex, aTiempo, formatearFecha } from './progresion.serie';

describe('progresion.serie', () => {
  it('interpreta yyyy-MM-dd en hora local, sin corrimiento por zona horaria', () => {
    const fecha = new Date(aTiempo('2026-10-01'));

    expect(fecha.getFullYear()).toBe(2026);
    expect(fecha.getMonth()).toBe(9);
    expect(fecha.getDate()).toBe(1);
  });

  it('arma la serie de ApexCharts con la fecha en ms y el peso en kg', () => {
    const serie = aSerieApex([
      { fecha: '2026-10-01', pesoMaximo: 80 },
      { fecha: '2026-10-08', pesoMaximo: 85 },
    ]);

    expect(serie).toEqual([
      { x: aTiempo('2026-10-01'), y: 80 },
      { x: aTiempo('2026-10-08'), y: 85 },
    ]);
  });

  it('conserva el orden ascendente que manda el backend', () => {
    const serie = aSerieApex([
      { fecha: '2026-01-05', pesoMaximo: 60 },
      { fecha: '2026-02-11', pesoMaximo: 70 },
      { fecha: '2026-03-20', pesoMaximo: 75 },
    ]);

    expect(serie.map((punto) => punto.y)).toEqual([60, 70, 75]);
    expect(serie[0].x).toBeLessThan(serie[1].x);
    expect(serie[1].x).toBeLessThan(serie[2].x);
  });

  it('formatea el valor del eje x como dd/MM/yyyy', () => {
    expect(formatearFecha(aTiempo('2026-03-09'))).toBe('09/03/2026');
  });

  it('devuelve vacío cuando el valor no es una fecha válida', () => {
    expect(formatearFecha('no-es-fecha')).toBe('');
  });
});
