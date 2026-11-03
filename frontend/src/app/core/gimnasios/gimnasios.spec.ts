import { TestBed } from '@angular/core/testing';
import { Gimnasios } from './gimnasios';

describe('Gimnasios', () => {
  let service: Gimnasios;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Gimnasios);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
