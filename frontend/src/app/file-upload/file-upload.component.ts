import {Component, Input, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {finalize} from "rxjs/operators";
import {HttpClient, HttpEventType} from "@angular/common/http";
import {Content} from "../content";

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {
  @Input()
  requiredFileType!:string;

  fileName = '';
  uploadProgress!:number | null;
  uploadSub!: Subscription | null;
  content!: Content;
  constructor(private http: HttpClient) { }

  ngOnInit(): void {
  }

  onFileSelected(event: any) {
    const file:File = event.target.files[0];

    if (file) {
      this.fileName = file.name;
      const formData = new FormData();
      formData.append("files", file);

      const upload$ = this.http.post("/api/content", formData, {
        reportProgress: true,
        observe: 'events'
      })
        .pipe(
          finalize(() => this.reset())
        );

      this.uploadSub = upload$.subscribe(uploadEvent => {
        if (uploadEvent.type == HttpEventType.UploadProgress) {
          // @ts-ignore
          this.uploadProgress = Math.round(100 * (uploadEvent.loaded / uploadEvent.total));
        }
        if (uploadEvent.type == HttpEventType.Response) {
          const contents = uploadEvent.body as Content[];
          this.content = contents[0];
        }
      })
    }
  }

  cancelUpload() {
    this.uploadSub?.unsubscribe();
    this.reset();
  }

  reset() {
    this.uploadProgress = null;
    this.uploadSub = null;
  }

}
