import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Musculo } from '../../model/musculo';
import { Page } from '../../model/page';

@Service()
export class MusculoService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/musculos';

  getAllMusculos(): Observable<Page<Musculo>> {
    return this.http.get<Page<Musculo>>(this.url, { params: { size: 100 } });
  }
}
