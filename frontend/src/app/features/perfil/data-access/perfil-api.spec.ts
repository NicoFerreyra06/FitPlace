import { TestBed } from '@angular/core/testing';
import { PerfilApi } from './perfil-api';

describe('PerfilApi', () => {
  let service: PerfilApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PerfilApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
