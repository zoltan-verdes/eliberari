import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Jurnal } from './jurnal';

describe('Jurnal', () => {
  let component: Jurnal;
  let fixture: ComponentFixture<Jurnal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Jurnal],
    }).compileComponents();

    fixture = TestBed.createComponent(Jurnal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
