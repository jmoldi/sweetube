import {Component, OnInit} from '@angular/core';
import {Content} from "../content";
import {ContentService} from "../content.service";
import {RequestStatus} from "../requestStatus";

@Component({
  selector: 'app-own-content-page',
  templateUrl: './own-content-page.component.html',
  styleUrls: ['./own-content-page.component.scss']
})
export class OwnContentPageComponent implements OnInit {
  contents!: Content[];
  status: RequestStatus= RequestStatus.INITIAL;

  constructor(private contentService: ContentService) { }

  ngOnInit(): void {
    this.getOwnContent();
  }

  getOwnContent(): void {

    this.contents=[];
    // Create observer object
    const myObserver = {
      next: (x: Content[]) => this.contents.push(...x),
      error: (err: Error) => {
        console.error('Observer got an error: ' + err);
        this.status=RequestStatus.ERROR;
      },
      complete: () => {
        console.log('Observer got a complete notification')
        this.status=RequestStatus.DONE;
      },
    };
    this.status = RequestStatus.LOADING;
    this.contentService.getOwnContent().subscribe(myObserver);
  }

}
