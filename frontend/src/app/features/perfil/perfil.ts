import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Usuario, UsuarioPerfil } from '../../core/usuario/usuario';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  imports: [DecimalPipe, ReactiveFormsModule, RouterLink],
  selector: 'app-perfil',
  styleUrl: './perfil.css',
  templateUrl: './perfil.html',
})
export class Perfil {
  private readonly usuarioService = inject(Usuario);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly errorMsg = signal<string | null>(null);
  protected readonly editando = signal<boolean>(false);

  formularioPerfil = this.formBuilder.nonNullable.group({
    peso: [0, [Validators.required, Validators.min(30), Validators.max(500)]],
    altura: [0, [Validators.required, Validators.min(0.5), Validators.max(3)]],
  });

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getMiPerfil(),
  });

  editar(p: UsuarioPerfil) {
    this.formularioPerfil.setValue({ peso: p.peso, altura: p.altura });
    this.editando.set(true);
  }

  guardar() {
    this.usuarioService.updateMiPerfil(this.formularioPerfil.getRawValue()).subscribe({
      next: (res) => {
        this.perfil.set(res);
        this.editando.set(false);
        this.errorMsg.set(null);
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor'),
    });
  }

  cancelar() {
    this.editando.set(false);
    this.errorMsg.set(null);
  }
}
