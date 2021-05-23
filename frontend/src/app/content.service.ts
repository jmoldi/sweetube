import { Injectable } from '@angular/core';
import {Observable, of, EMPTY} from "rxjs";
import {Content} from "./content";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private url = '/api/content/own';  // URL to web api

  constructor(private http: HttpClient) { }

  getOwnContent(): Observable<Content[]> {
    return this.http.get<Content[]>(this.url);
  }
}
