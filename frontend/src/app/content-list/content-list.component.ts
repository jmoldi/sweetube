import {AfterViewInit,Component, OnInit, ViewChild} from '@angular/core';
import { CONTENTS } from '../mock-content';
import { MediaChange, MediaObserver } from '@angular/flex-layout';
import {MatGridList} from "@angular/material/grid-list";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-content-list',
  templateUrl: './content-list.component.html',
  styleUrls: ['./content-list.component.scss']
})
export class ContentListComponent implements OnInit {
  contents = CONTENTS;
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

  constructor(private mediaObserver: MediaObserver) {  }

  ngOnInit(): void {
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

}
