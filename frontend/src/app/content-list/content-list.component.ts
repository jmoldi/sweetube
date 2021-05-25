import {AfterViewInit,Component, OnInit, ViewChild} from '@angular/core';
import { MediaChange, MediaObserver } from '@angular/flex-layout';
import {MatGridList} from "@angular/material/grid-list";
import {Observable, Subscription} from "rxjs";
import {ContentService} from "../content.service";
import {Content} from "../content";
import {RequestStatus} from "../requestStatus";

@Component({
  selector: 'app-content-list',
  templateUrl: './content-list.component.html',
  styleUrls: ['./content-list.component.scss']
})
export class ContentListComponent implements OnInit {
  contents!: Observable<Content[]>;
  status: RequestStatus= RequestStatus.INITIAL;
  @ViewChild('grid') grid!: MatGridList;
  gridByBreakpoint: { [key: string]: number; } = {
    xl: 5,
    lg: 4,
    md: 3,
    sm: 2,
    xs: 1
  }
  activeMediaQuery = '';
  watcher!: Subscription;

  constructor(private mediaObserver: MediaObserver, private contentService: ContentService) {  }

  ngOnInit(): void {
    this.getContent();
  }

  ngAfterViewInit() {
    // child is set
    if(this.grid){
      this.grid.cols= 6;
    }
    this.watcher = this.mediaObserver.asObservable().subscribe((changes: MediaChange[]) => {
      const change = changes[0];
      this.activeMediaQuery = change ? `'${change.mqAlias}' = (${change.mediaQuery})` : '';
      this.grid.cols = this.gridByBreakpoint[change.mqAlias];
    })
  }

  ngOnDestroy(){
    this.watcher.unsubscribe();
  }

  getContent(): void {

    /*this.contents=[];
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
    this.contentService.getContent().subscribe(myObserver);*/

    this.contents = this.contentService.getContent();
  }

}
