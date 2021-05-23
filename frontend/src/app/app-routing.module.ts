import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ContentListComponent} from "./content-list/content-list.component";
import {EmptyComponent} from "./empty-component/empty.component";
import {OwnContentPageComponent} from "./own-content-page/own-content-page.component";

const routes: Routes = [
  { path: '', component: ContentListComponent },
  { path: 'me', component: OwnContentPageComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
