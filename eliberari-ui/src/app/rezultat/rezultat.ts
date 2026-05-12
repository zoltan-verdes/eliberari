import { Component, computed, Input, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
// import * as JSZip from 'jszip';

@Component({
  selector: 'app-rezultat',
  imports: [],
  templateUrl: './rezultat.html',
  styleUrl: './rezultat.scss',
})

export class Rezultat {
  @Input() date: any[] = [];
  rezultate = signal<any[]>([]);
//  zipOriginal: JSZip | null = null;
  private curentUrl: string | null = null;

  formatStatus(status: any): string {
    if (!status || (status.ci === 0)) return ' - ';
    if (!status || (status.cim === 0)) return '-';
    if (!status || (status.cc === 0)) return '-';

    return `${status.nrPagLot} / ${status.nrPagScanat}`;
  }




/*
async selecteazaDocument(file: File, numeDocument: string) {
  const jszip = new JSZip();
  this.zipOriginal = await jszip.loadAsync(file);
// Eliberăm URL-ul vechi dacă există
  if (this.curentUrl) {
    URL.revokeObjectURL(this.curentUrl);
  }

  if (!this.zipOriginal) return;

  const fileEntry = this.zipOriginal.file(numeDocument);
  
  if (fileEntry) {
    // Extragerea propriu-zisă are loc DOAR ACUM
    const blob = await fileEntry.async("blob");
    
    // Creăm URL-ul pentru afișare
    const documentUrl = URL.createObjectURL(blob);
    
    // Transmiți documentUrl către semnalul sau variabila folosită de <pdf-viewer>
   // this.pdfSrc.set(documentUrl);
  }
}  */
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
