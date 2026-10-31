import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { Amigos, AmigoResponse } from '../../../core/amigos/amigos';
import { Usuario } from '../../../core/usuario/usuario';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-mis-amigos',
  styleUrl: './mis-amigos.css',
  templateUrl: './mis-amigos.html',
})
export class MisAmigos implements OnInit {
  private readonly amigosService = inject(Amigos);
  private readonly usuarioService = inject(Usuario);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly cargando = signal(true);
  protected readonly amigos = signal<AmigoResponse[]>([]);
  protected readonly miCodigo = signal<string | null>(null);
  protected readonly errorMsg = signal<string | null>(null);
  protected readonly mensajeExito = signal<string | null>(null);
  protected readonly agregando = signal(false);
  protected readonly eliminandoId = signal<number | null>(null);

  protected readonly totalAmigos = computed(() => this.amigos().length);

  protected readonly formulario = this.formBuilder.nonNullable.group({
    codigoAmigo: ['', [Validators.required, Validators.maxLength(64)]],
  });

  ngOnInit() {
    this.cargarDatos();
  }

  private cargarDatos() {
    this.cargando.set(true);

    forkJoin({
      amigos: this.amigosService.getAmigos(),
      perfil: this.usuarioService.getPerfilGuardado(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ amigos, perfil }) => {
          this.amigos.set(this.ordenarAmigos(amigos));
          this.miCodigo.set(perfil.codigoAmigo);
          this.cargando.set(false);
        },
        error: (err) => {
          this.errorMsg.set(err.error?.message ?? 'No se pudo cargar tu grupo de amigos');
          this.cargando.set(false);
        },
      });
  }

  protected agregar() {
    if (this.agregando()) {
      return;
    }

    const control = this.formulario.controls.codigoAmigo;
    control.markAsTouched();

    const codigo = control.value.trim();

    if (!codigo) {
      control.setErrors({ required: true });
      return;
    }

    this.agregando.set(true);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.amigosService
      .agregarAmigo(codigo)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.formulario.reset({ codigoAmigo: '' });
          this.mensajeExito.set('¡Amigo agregado a tu grupo!');
          this.agregando.set(false);
          this.refrescarAmigos();
        },
        error: (err) => {
          this.agregando.set(false);
          this.errorMsg.set(err.error?.message ?? 'No se pudo agregar el amigo');
        },
      });
  }

  protected eliminar(amigo: AmigoResponse) {
    if (!confirm(`¿Seguro que querés eliminar a ${amigo.username} de tu grupo?`)) {
      return;
    }

    this.eliminandoId.set(amigo.id);
    this.errorMsg.set(null);
    this.mensajeExito.set(null);

    this.amigosService
      .eliminarAmigo(amigo.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.amigos.update((lista) => lista.filter((a) => a.id !== amigo.id));
          this.eliminandoId.set(null);
          this.mensajeExito.set(`Eliminaste a ${amigo.username} de tu grupo.`);
        },
        error: (err) => {
          this.eliminandoId.set(null);
          this.errorMsg.set(err.error?.message ?? 'No se pudo eliminar el amigo');
        },
      });
  }

  protected async copiarCodigo() {
    const codigo = this.miCodigo();

    if (!codigo) {
      return;
    }

    try {
      await navigator.clipboard.writeText(codigo);
      this.mensajeExito.set('Código copiado. ¡Compartilo para que te agreguen!');
    } catch {
      this.errorMsg.set('No se pudo copiar el código. Copialo manualmente.');
    }
  }

  protected etiquetaRol(rol: string): string {
    switch (rol) {
      case 'ENTRENADOR':
        return 'Entrenador';
      case 'ADMIN':
        return 'Administrador';
      case 'ADMIN_GIMNASIO':
        return 'Admin de gimnasio';
      default:
        return 'Usuario';
    }
  }

  private refrescarAmigos() {
    this.amigosService
      .getAmigos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (amigos) => this.amigos.set(this.ordenarAmigos(amigos)),
        error: () =>
          this.errorMsg.set('El amigo se agregó, pero no se pudo actualizar la lista.'),
      });
  }

  private ordenarAmigos(amigos: AmigoResponse[]): AmigoResponse[] {
    return [...amigos].sort((a, b) => a.username.localeCompare(b.username, 'es'));
  }
}
