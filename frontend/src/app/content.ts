import {Creator} from "./creator";
import {DateTime} from "luxon";

export interface Content {
  id: number;
  name: string;
  url: string;
  thumbnail: string;
  creator: Creator;
  views: number;
  createdAt: DateTime;
}
