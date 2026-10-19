import { Component, inject, signal } from '@angular/core';
import { Auth } from '../../../core/auth/auth';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-registro',
  styleUrl: './registro.css',
  templateUrl: './registro.html',
})
export class Registro {
  private readonly authService = inject(Auth);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  formularioRegistro = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    peso: [0, [Validators.required, Validators.min(30), Validators.max(500)]],
    altura: [0, [Validators.required, Validators.min(0.5), Validators.max(3.0)]],
  });

  protected readonly errorMsg = signal<string | null>(null);

  onSubmit() {
    this.authService.register(this.formularioRegistro.getRawValue()).subscribe({
      next: () => this.router.navigate(['/login']),
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor'),
    });
  }
}
