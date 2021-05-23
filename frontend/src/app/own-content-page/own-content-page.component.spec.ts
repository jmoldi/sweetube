import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnContentPageComponent } from './own-content-page.component';

describe('OwnContentPageComponent', () => {
  let component: OwnContentPageComponent;
  let fixture: ComponentFixture<OwnContentPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OwnContentPageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OwnContentPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
