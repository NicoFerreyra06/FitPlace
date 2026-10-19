import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PerfilApi } from '../../data-access/perfil-api';

@Component({
  imports: [DecimalPipe, RouterLink],
  selector: 'app-perfil',
  styleUrl: './perfil.css',
  templateUrl: './perfil.html',
})
export class Perfil {



 private readonly perfilApi = inject(PerfilApi);

  protected readonly perfil = rxResource({
    stream: () => this.perfilApi.getMiPerfil(),
  });

  protected readonly claseImc = computed(() => {
    if (!this.perfil.hasValue()) return '';
    switch (this.perfil.value().categoriaImc) {
      case 'Peso Normal':
        return 'estado-ok';
      case 'Bajo peso':
      case 'Exceso de peso':
        return 'estado-alerta';
      case null:
        return '';
      default:
        return 'estado-riesgo';
    }
  });


}
