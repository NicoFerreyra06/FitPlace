import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { GestionGimnasio } from './gestion-gimnasio';
import { Gimnasios } from '../../../core/gimnasios/gimnasios';
import { Suscripciones } from '../../../core/suscripciones/suscripciones';

describe('GestionGimnasio', () => {
  let fixture: ComponentFixture<GestionGimnasio>;
  let router: Router;
  let gimnasios: {
    getGimnasio: ReturnType<typeof vi.fn>;
    getMiembros: ReturnType<typeof vi.fn>;
    actualizarGimnasio: ReturnType<typeof vi.fn>;
    eliminarGimnasio: ReturnType<typeof vi.fn>;
  };

  const gimnasio = {
    id: 7,
    nombre: 'Mi gimnasio',
    direccion: 'Calle 123',
    precioCuota: 1000,
    idAdmin: 1,
    nombreAdmin: 'maure',
    diasDeApertura: ['MONDAY', 'WEDNESDAY'],
    horarioApertura: '08:00:00',
    horarioCierre: '20:00:00',
    fotoUrl: null,
    isMercadoPagoVinculado: false,
  };

  beforeEach(async () => {
    gimnasios = {
      getGimnasio: vi.fn(() => of(gimnasio)),
      getMiembros: vi.fn(() => of([])),
      actualizarGimnasio: vi.fn(() => of(gimnasio)),
      eliminarGimnasio: vi.fn(() => of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [GestionGimnasio],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: new Map([['id', '7']]) } },
        },
        { provide: Gimnasios, useValue: gimnasios },
        {
          provide: Suscripciones,
          useValue: { getSuscripcionesGimnasio: () => of([]) },
        },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(GestionGimnasio);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('updates the gym from the edit form', () => {
    const root = fixture.nativeElement as HTMLElement;
    const botonEditar = root.querySelector<HTMLButtonElement>('.administrar > .acciones .btn--contorno');
    if (!botonEditar) throw new Error('Edit button was not rendered');
    botonEditar.click();
    fixture.detectChanges();

    const nombre = root.querySelector<HTMLInputElement>('#nombre-gimnasio');
    if (!nombre) throw new Error('Gym name input was not rendered');
    nombre.value = 'Gimnasio actualizado';
    nombre.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form = root.querySelector<HTMLFormElement>('form');
    if (!form) throw new Error('Edit form was not rendered');
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

    expect(gimnasios.actualizarGimnasio).toHaveBeenCalledWith(7, {
      nombre: 'Gimnasio actualizado',
      direccion: 'Calle 123',
      precioCuota: 1000,
      horarioApertura: '08:00',
      horarioCierre: '20:00',
      diasAbierto: ['MONDAY', 'WEDNESDAY'],
      fotoUrl: null,
    });
  });

  it('asks for confirmation before deleting the gym', () => {
    const root = fixture.nativeElement as HTMLElement;
    const botonEliminar = root.querySelector<HTMLButtonElement>('.administrar > .acciones .btn--peligro');
    if (!botonEliminar) throw new Error('Delete button was not rendered');
    botonEliminar.click();
    fixture.detectChanges();

    expect(root.querySelector('[role="alertdialog"]')).toBeTruthy();
    expect(gimnasios.eliminarGimnasio).not.toHaveBeenCalled();

    const confirmar = root.querySelector<HTMLButtonElement>('.confirmacion .btn--peligro');
    if (!confirmar) throw new Error('Delete confirmation button was not rendered');
    confirmar.click();

    expect(gimnasios.eliminarGimnasio).toHaveBeenCalledWith(7);
    expect(router.navigate).toHaveBeenCalledWith(['/gimnasios']);
  });
});
