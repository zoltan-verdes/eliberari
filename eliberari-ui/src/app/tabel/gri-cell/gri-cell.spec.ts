import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GriCell } from './gri-cell';

describe('GriCell', () => {
  let component: GriCell;
  let fixture: ComponentFixture<GriCell>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GriCell],
    }).compileComponents();

    fixture = TestBed.createComponent(GriCell);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
