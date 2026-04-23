import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GriRow } from './gri-row';

describe('GriRow', () => {
  let component: GriRow;
  let fixture: ComponentFixture<GriRow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GriRow],
    }).compileComponents();

    fixture = TestBed.createComponent(GriRow);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
