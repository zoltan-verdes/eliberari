import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import * as pdfjsLib from 'pdfjs-dist';

(pdfjsLib as any).GlobalWorkerOptions.workerSrc =
  `${window.location.origin}/assets/pdfjs/pdf.worker.min.mjs`;

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
