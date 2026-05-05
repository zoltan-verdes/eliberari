import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PdfViewerModule, PDFDocumentProxy } from 'ng2-pdf-viewer';

@Component({
  selector: 'app-pdf-validator',
  standalone: true,
  imports: [CommonModule, PdfViewerModule],
  templateUrl: './pdf-validator.component.html',
  styleUrl: './pdf-validator.component.css'
})
export class PdfValidatorComponent {
  // Primim fișierul și starea paginilor de la părinte
  pdfSrc = input.required<File | string | Uint8Array | null>();
  pageStatuses = input.required<boolean[]>(); // true = păstrat, false = ignorat
  
  // Trimitem modificările înapoi la părinte
  statusChanged = output<boolean[]>();

  totalPages = signal<number>(0);

  // Se execută când PDF-ul este încărcat cu succes de bibliotecă
  onPdfLoaded(pdf: PDFDocumentProxy) {
    this.totalPages.set(pdf.numPages);
  }

  togglePage(index: number) {
    const updated = [...this.pageStatuses()];
    updated[index] = !updated[index];
    this.statusChanged.emit(updated);
  }
}