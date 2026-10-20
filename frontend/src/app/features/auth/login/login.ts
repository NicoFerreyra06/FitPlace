import { Component, inject, signal } from '@angular/core';
import { Validators, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { Auth } from '../../../core/auth/auth';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(Auth);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  formularioLogin = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected readonly errorMsg = signal<string | null>(null);

  onSubmit() {
    this.authService.login(this.formularioLogin.getRawValue()).subscribe({
      next: (res) => {
        this.errorMsg.set(null);
        this.authService.guardarToken(res.token);
        this.router.navigate(['/home']);
      },
      error: (err) =>
        this.errorMsg.set(err.error?.message ?? 'No se pudo conectar con el servidor'),
    });
  }
}
