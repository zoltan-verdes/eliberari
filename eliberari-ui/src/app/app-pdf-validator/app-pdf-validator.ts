import { Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PdfViewerModule, PDFDocumentProxy } from 'ng2-pdf-viewer';

@Component({
  selector: 'app-pdf-validator',
  standalone: true,
  imports: [CommonModule, PdfViewerModule],
  templateUrl: './app-pdf-validator.html',
  styleUrl: './app-pdf-validator.scss'
})
export class PdfValidatorComponent {
  // Primim fișierul și starea paginilor de la părinte

  fileInput = input.required<File | null>();

  // Conversia are loc aici, automat, ori de câte ori fileInput se schimbă
  pdfSrc = computed(() => {
    const file = this.fileInput();
    if (file instanceof File) {
      return URL.createObjectURL(file);
    }
    return null;
  });


//  pdfSrc = input.required<File | string | Uint8Array | null>();
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

  ngOnDestroy() {
    const currentUrl = this.pdfSrc();
    if (currentUrl) {
      URL.revokeObjectURL(currentUrl);
    }
  }


}