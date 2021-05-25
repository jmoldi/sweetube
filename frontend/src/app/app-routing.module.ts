import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ContentListComponent} from "./content-list/content-list.component";
import {OwnContentPageComponent} from "./own-content-page/own-content-page.component";
import {ContentPageComponent} from "./content-page/content-page.component";
import {FileUploadComponent} from "./file-upload/file-upload.component";
import {EmptyComponent} from "./empty-component/empty.component";

const routes: Routes = [
  { path: '', component: ContentListComponent },
  { path: 'me', component: OwnContentPageComponent },
  { path: 'create', component: FileUploadComponent },
  { path: 'v/:id', component: ContentPageComponent },
  { path: 'user/:id', component: EmptyComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
