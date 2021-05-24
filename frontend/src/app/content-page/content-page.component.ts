import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {Content} from "../content";
import {ContentService} from "../content.service";
import {RequestStatus} from "../requestStatus";

@Component({
  selector: 'app-content-page',
  templateUrl: './content-page.component.html',
  styleUrls: ['./content-page.component.scss']
})
export class ContentPageComponent implements OnInit {
  @Input() content!: Content;
  status!: RequestStatus;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private contentService: ContentService) {
    this.route.params.subscribe( params => {
      const state = router.getCurrentNavigation()?.extras.state;
      if(state && state.content){
        this.content = state.content;
      } else {
        contentService.getContentById(params.id);
        // Create observer object
        const myObserver = {
          next: (x: Content) => this.content = x,
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
        this.contentService.getContentById(params.id).subscribe(myObserver);
      }
    } );
  }

  ngOnInit(): void {
  }

}
