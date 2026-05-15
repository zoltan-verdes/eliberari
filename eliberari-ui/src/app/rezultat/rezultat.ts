import { Component, computed, inject, Input, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CerereItem } from '../../model';
import { PdfService } from '../pdf.service';
// import * as JSZip from 'jszip';

@Component({
  selector: 'app-rezultat',
  imports: [],
  templateUrl: './rezultat.html',
  styleUrl: './rezultat.scss',
})

export class Rezultat {
  @Input() date: CerereItem[] = []; 
  private pdfService = inject(PdfService);


mergiLaPagina(paginaPropusa: number) {
  this.pdfService.sariLaPagina(paginaPropusa);
    
}

  formatStatus(valoare: any): string {
    if (valoare === 0 || valoare === null || valoare === undefined) return '-';
    return valoare.toString();
  }

}


/*
  niste cod legacy cu comunicare SSE

    pornesteProcesare() {
    this.logs.add('Inițializare conexiune...');
    this.rezultate.set([]);

    const eventSource = new EventSource('http://localhost:8080/api/ocr/stream');

    eventSource.onmessage = (event) => {
      this.zone.run(() => {
        // event.data contine textul trimis de sseListener.onLog
        this.logs.addLogs(event.data);
      });
    };

    // Prindem evenimentul special pentru tabel
    eventSource.addEventListener('tabel', (event: any) => {
      this.zone.run(() => {
        this.rezultate.set(JSON.parse(event.data));
      });
    });

    //Prindem evenimentul de stream imagine

    eventSource.onerror = (error) => {
      this.zone.run(() => {
        console.log('Procesare finalizată sau întreruptă.');
        eventSource.close();
        this.isProcessing.set(false);
      });
    };
  }
*/
