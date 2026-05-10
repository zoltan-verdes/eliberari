import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IncarcaLot } from './incarca-lot';

describe('IncarcaLot', () => {
  let component: IncarcaLot;
  let fixture: ComponentFixture<IncarcaLot>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IncarcaLot],
    }).compileComponents();

    fixture = TestBed.createComponent(IncarcaLot);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
