import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// ng2-pdf-viewer foloseste propria copie de pdfjs-dist si, in lipsa lui
// window.pdfWorkerSrc, cade pe varianta CDN (jsdelivr) la construirea PdfViewerComponent.
// Setam aici calea locala, copiata in assets/pdfjs de angular.json, ca sa functioneze offline.
const baseHref = document.querySelector('base')?.getAttribute('href') || '/';
(window as any).pdfWorkerSrc = (baseHref + '/assets/pdfjs/pdf.worker.min.mjs').replace(/\/+/g, '/');

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
