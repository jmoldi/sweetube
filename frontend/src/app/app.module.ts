import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatCardModule} from '@angular/material/card';
import { ContentListComponent } from './content-list/content-list.component';
import { ContentPreviewComponent } from './content-preview/content-preview.component';
import {MatGridListModule} from '@angular/material/grid-list';
import {LuxonModule} from "luxon-angular";

@NgModule({
  declarations: [
    AppComponent,
    ContentListComponent,
    ContentPreviewComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule,
    MatToolbarModule,
    MatCardModule,
    MatGridListModule,
    LuxonModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
