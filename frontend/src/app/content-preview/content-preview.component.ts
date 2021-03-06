import {Component, Input, OnInit} from '@angular/core';
import {Content} from "../content";
import {interval, Observable} from "rxjs";
import {distinctUntilChanged, map} from "rxjs/operators";

@Component({
  selector: 'app-content-preview',
  templateUrl: './content-preview.component.html',
  styleUrls: ['./content-preview.component.scss']
})

export class ContentPreviewComponent implements OnInit {
  @Input() content!: Content;

  constructor() { }

  ngOnInit(): void {
  }

}

