import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentPreviewComponent } from './content-preview.component';
import {DateTimeToRelativePipe} from "luxon-angular";

describe('ContentPreviewComponent', () => {
  let component: ContentPreviewComponent;
  let fixture: ComponentFixture<ContentPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContentPreviewComponent, DateTimeToRelativePipe ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContentPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
