import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  imports: [RouterLink],
  selector: 'app-not-found',
  styleUrl: './not-found.css',
  templateUrl: './not-found.html',
})
export class NotFound {
  private readonly location = inject(Location);

  volver() {
    this.location.back();
  }
}
