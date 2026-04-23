import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GriTable } from './gri-table';

describe('GriTable', () => {
  let component: GriTable;
  let fixture: ComponentFixture<GriTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GriTable],
    }).compileComponents();

    fixture = TestBed.createComponent(GriTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
