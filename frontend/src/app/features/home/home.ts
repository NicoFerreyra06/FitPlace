import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../../core/auth/auth';

@Component({
  imports: [],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {

  private readonly router = inject(Router);
  private readonly auth = inject(Auth);

  cerrarSesion(){
    this.auth.logOut();
    this.router.navigate(['/login']);
  }
}
