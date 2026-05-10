import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IncarcaScanat } from './incarca-scanat';

describe('IncarcaScanat', () => {
  let component: IncarcaScanat;
  let fixture: ComponentFixture<IncarcaScanat>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IncarcaScanat],
    }).compileComponents();

    fixture = TestBed.createComponent(IncarcaScanat);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
