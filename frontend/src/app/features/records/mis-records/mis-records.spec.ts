import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MisRecords } from './mis-records';

describe('MisRecords', () => {
  let component: MisRecords;
  let fixture: ComponentFixture<MisRecords>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisRecords],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(MisRecords);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
