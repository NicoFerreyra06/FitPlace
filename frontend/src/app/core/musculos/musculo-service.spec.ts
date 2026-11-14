import { TestBed } from '@angular/core/testing';
import { MusculoService } from './musculo-service';

describe('MusculoService', () => {
  let service: MusculoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MusculoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
