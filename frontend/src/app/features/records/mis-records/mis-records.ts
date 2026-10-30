import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { Usuario } from '../../../core/usuario/usuario';
import { Records } from '../../../core/records/records';

@Component({
  imports: [DatePipe, FormsModule],
  selector: 'app-mis-records',
  styleUrl: './mis-records.css',
  templateUrl: './mis-records.html',
})
export class MisRecords {
  private readonly usuarioService = inject(Usuario);
  private readonly recordsService = inject(Records);

  protected readonly busqueda = signal<string>('');

  protected readonly perfil = rxResource({
    stream: () => this.usuarioService.getPerfilGuardado(),
  });

  protected readonly records = rxResource({
    params: () => (this.perfil.hasValue() ? { idUsuario: this.perfil.value().id } : undefined),
    stream: ({ params }) => this.recordsService.getMisRecords(params.idUsuario),
  });

  protected readonly recordsFiltrados = computed(() => {
    const lista = this.records.value() ?? [];
    const termino = this.busqueda().trim().toLowerCase();

    if (!termino) {
      return lista;
    }

    return lista.filter((record) => record.nombreEjercicio?.toLowerCase().includes(termino));
  });

  protected get cargando(): boolean {
    return this.perfil.isLoading() || (this.records.isLoading() && !this.records.hasValue());
  }

  protected get hayError(): boolean {
    return Boolean(this.perfil.error() || this.records.error());
  }

  protected reintentar(): void {
    if (this.perfil.error()) {
      this.perfil.reload();
    }
    if (this.records.error()) {
      this.records.reload();
    }
  }

  protected limpiarBusqueda(): void {
    this.busqueda.set('');
  }
}
