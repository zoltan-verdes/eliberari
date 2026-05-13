import { Component, computed, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PdfViewerModule, PDFDocumentProxy } from 'ng2-pdf-viewer';
import { PdfService } from '../pdf.service';

@Component({
  selector: 'app-pdf-validator',
  standalone: true,
  imports: [CommonModule, PdfViewerModule],
  templateUrl: './app-pdf-validator.html',
  styleUrl: './app-pdf-validator.scss'
})
export class PdfValidatorComponent {

  private pdfService = inject(PdfService);
  fileInput = this.pdfService.doc;
  pageStatuses = this.pdfService.pageStatuses;

  


  
  // Primim fișierul și starea paginilor de la părinte
   
//  fileInput = input.required<File | null>();

  // Conversia are loc aici, automat, ori de câte ori fileInput se schimbă
  pdfSrc = computed(() => {
    const file = this.fileInput();
    if (file instanceof File) {
      return URL.createObjectURL(file);
    }
    return null;
  });



//  pdfSrc = input.required<File | string | Uint8Array | null>();
 // pageStatuses = input.required<boolean[]>(); // true = păstrat, false = ignorat
  
  // Trimitem modificările înapoi la părinte
  statusChanged = output<boolean[]>();

  totalPages = signal<number>(0);


onPdfLoaded(pdf: PDFDocumentProxy) {
  console.log('PDF interogat cu succes. Pagini:', pdf.numPages);
  
  // Update-ul semnalului trebuie să fie imediat
  this.totalPages.set(pdf.numPages);

  // IMPORTANT: Dacă din backend au venit deja statusurile (din incarca-scanat), 
  // nu le suprascrie cu Array(fill.true). 
  // Verificăm dacă lungimea curentă corespunde cu numărul de pagini.
  if (this.pageStatuses().length !== pdf.numPages) {
    console.log('Inițializare statusuri pagini implicit (toate true)');
    this.pdfService.pageStatuses.set(new Array(pdf.numPages).fill(true));
  }
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