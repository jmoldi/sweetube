import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ContentListComponent} from "./content-list/content-list.component";

const routes: Routes = [
  { path: '', component: ContentListComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
