import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdatebankComponent } from './updatebank.component';

describe('UpdatebankComponent', () => {
  let component: UpdatebankComponent;
  let fixture: ComponentFixture<UpdatebankComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdatebankComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdatebankComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
