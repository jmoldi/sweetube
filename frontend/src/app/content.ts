import {Creator} from "./creator";
import {DateTime} from "luxon";

export interface Content {
  id: string;
  name: string;
  contentId: string;
  thumbnailId: string;
  previewId: string;
  creator: Creator;
  views: number;
  createdAt: string;
}
