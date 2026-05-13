import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoveziRidicare } from './dovezi-ridicare';

describe('DoveziRidicare', () => {
  let component: DoveziRidicare;
  let fixture: ComponentFixture<DoveziRidicare>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoveziRidicare],
    }).compileComponents();

    fixture = TestBed.createComponent(DoveziRidicare);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
