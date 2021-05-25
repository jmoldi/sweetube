import {Component, Input, OnInit} from '@angular/core';
import {Content} from "../content";

@Component({
  selector: 'app-content-preview',
  templateUrl: './content-preview.component.html',
  styleUrls: ['./content-preview.component.scss']
})

export class ContentPreviewComponent implements OnInit {
  @Input() content!: Content;
  imgSrc: string | undefined;

  constructor() { }

  ngOnInit(): void {
    this.mouseLeave();
  }

  mouseEnter() {
    this.imgSrc = "/api/content/"+this.content.previewId+"/stream";
  }

  mouseLeave() {
    this.imgSrc = "/api/content/"+this.content.thumbnailId+"/stream";
  }
}

