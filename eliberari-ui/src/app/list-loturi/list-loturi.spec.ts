import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListLoturi } from './list-loturi';

describe('ListLoturi', () => {
  let component: ListLoturi;
  let fixture: ComponentFixture<ListLoturi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListLoturi],
    }).compileComponents();

    fixture = TestBed.createComponent(ListLoturi);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
