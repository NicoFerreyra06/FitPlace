import { TestBed } from '@angular/core/testing';
import { Records } from './records';

describe('Records', () => {
  let service: Records;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Records);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
