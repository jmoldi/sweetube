import { Injectable } from '@angular/core';
import {Observable, of, EMPTY} from "rxjs";
import {Content} from "./content";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private urlOwn = '/api/content/own';  // URL to web api own content
  private urlAll = '/api/content';  // URL to web api all content
  private urlId = '/api/content';  // URL to web api all content

  constructor(private http: HttpClient) { }

  getOwnContent(): Observable<Content[]> {
    return this.http.get<Content[]>(this.urlOwn);
  }

  getContent(): Observable<Content[]> {
    return this.http.get<Content[]>(this.urlAll);
  }

  getContentById(id: String): Observable<Content> {
    return this.http.get<Content>(this.urlId + `/${id}`);
  }
}
