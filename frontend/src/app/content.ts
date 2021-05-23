import {Creator} from "./creator";
import {DateTime} from "luxon";

export interface Content {
  id: string;
  name: string;
  content: {
    url: string
  };
  thumbnail: {
    url: string
  };
  preview: {
    url: string
  };
  creator: Creator;
  views: number;
  createdAt: DateTime;
}
