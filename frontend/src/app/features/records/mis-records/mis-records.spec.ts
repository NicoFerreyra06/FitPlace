import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MisRecords } from './mis-records';

describe('MisRecords', () => {
  let component: MisRecords;
  let fixture: ComponentFixture<MisRecords>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisRecords],
    }).compileComponents();

    fixture = TestBed.createComponent(MisRecords);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
