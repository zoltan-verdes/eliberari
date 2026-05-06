import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppPdfValidator } from './app-pdf-validator';

describe('AppPdfValidator', () => {
  let component: AppPdfValidator;
  let fixture: ComponentFixture<AppPdfValidator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppPdfValidator],
    }).compileComponents();

    fixture = TestBed.createComponent(AppPdfValidator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
