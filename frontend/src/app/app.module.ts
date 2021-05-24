import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatCardModule} from '@angular/material/card';
import { ContentListComponent } from './content-list/content-list.component';
import { ContentPreviewComponent } from './content-preview/content-preview.component';
import {MatGridListModule} from '@angular/material/grid-list';
import {LuxonModule} from "luxon-angular";
import { EmptyComponent } from './empty-component/empty.component';
import { OwnContentPageComponent } from './own-content-page/own-content-page.component';
import { HttpClientModule } from '@angular/common/http';
import { ContentPageComponent } from './content-page/content-page.component';
import {StreamPipe} from "./streamPipe";
import { FileUploadComponent } from './file-upload/file-upload.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';

@NgModule({
  declarations: [
    AppComponent,
    ContentListComponent,
    ContentPreviewComponent,
    EmptyComponent,
    OwnContentPageComponent,
    ContentPageComponent,
    StreamPipe,
    FileUploadComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule,
    MatSidenavModule,
    MatToolbarModule,
    MatCardModule,
    MatProgressBarModule,
    MatGridListModule,
    LuxonModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
