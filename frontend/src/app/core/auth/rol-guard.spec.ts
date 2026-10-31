import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { rolGuard } from './rol-guard';

describe('rolGuardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => rolGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
