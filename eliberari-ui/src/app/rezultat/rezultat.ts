import { Component, Input } from '@angular/core';
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
//  zipOriginal: JSZip | null = null;
  private curentUrl: string | null = null;

  formatStatus(status: any): string {
    if (!status || (status.nrPagLot === 0 && status.nrPagScanat === 0)) return '- / -';
    if (!status || (status.nrPagLot === 0)) return '- / '+  status.nrPagScanat;
    if (!status || (status.nrPagScanat === 0)) return status.nrPagLot + ' / -';

    return `${status.nrPagLot} / ${status.nrPagScanat}`;
  }

  isComplet(status: any): boolean {
    return status?.complet ?? true;
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
