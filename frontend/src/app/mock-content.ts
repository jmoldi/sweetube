import { Content } from './content';
import {DateTime} from "luxon";
const creator = {id:'1', name: 'dhana', avatar:'https://image.flaticon.com/icons/png/512/194/194938.png'};
const vid= {
  thumbnail: {url:"https://picsum.photos/seed/{{content.id}}/344/193"},
  preview: {url:"https://picsum.photos/seed/{{content.id}}/344/193"},
  content: {url:"https://vjs.zencdn.net/v/oceans.mp4"}
};
const createdAtTime : DateTime = DateTime.fromISO("2021-05-13T09:10:23");

export const CONTENTS: Content[] = [
  { id: '11', name: 'Dr Nice', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '12', name: 'Narco', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '13', name: 'Bombasto', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '14', name: 'Celeritas', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '15', name: 'Magneta', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '16', name: 'RubberMan', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '17', name: 'Dynama', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '18', name: 'Dr IQ', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '19', name: 'Magma', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
  { id: '20', name: 'Tornado', ...vid, views: 10, createdAt: createdAtTime, creator: creator},
];
