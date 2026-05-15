import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { PdfService } from '../pdf.service';

@Component({
  selector: 'app-pdf-validator',
  standalone: true,
  imports: [CommonModule, PdfViewerModule],
  templateUrl: './app-pdf-validator.html',
  styleUrl: './app-pdf-validator.scss'
})
export class PdfValidatorComponent {
  public pdfService = inject(PdfService);
  
  // Sursă reactivă care se curăță automat
  localPdfSrc = computed(() => {
    const file = this.pdfService.doc();
    return file instanceof File ? URL.createObjectURL(file) : null;
  });

// Legăm pagina din viewer de semnalul din service
  paginaDeAfisat = this.pdfService.paginaCurenta;  

onPageRendered(event: any) {
  console.log('NG2-PDF-VIEWER EVENT:', event);
/*  
  const divPagina = event.div || event.element || event.source?.div;
  const nrPagina = event.pageNumber || event.page || event.pageIndex + 1;

  console.log('Număr pagină identificat:', nrPagina);
  console.log('Element HTML identificat:', divPagina);

  if (nrPagina && divPagina) {
    const statuses = this.pdfService.pageStatuses();
    const esteAnulata = !!statuses[nrPagina - 1];

    if (esteAnulata) {
      divPagina.classList.add('pagina-anulata');
    } else {
      divPagina.classList.remove('pagina-anulata');
    }
  }

*/

//  const nrPagina = event.pageNumber || event.page || (event._pageIndex !== undefined ? event._pageIndex + 1 : null);
  const nrPagina = event.pageNumber;

  // Căutăm direct în DOM div-ul paginii randerate care are atributul specificat
  if (nrPagina) {
    const divPagina = document.querySelector(`.pdfViewer .page[data-page-number="${nrPagina}"]`) as HTMLElement;

    if (nrPagina && divPagina) {
      const statuses = this.pdfService.pageStatuses();
      const esteAnulata = !!statuses[nrPagina - 1];

      if (esteAnulata) {
        divPagina.classList.add('pagina-anulata');
      } else {
        divPagina.classList.remove('pagina-anulata');
      }

      divPagina.onclick = () => {
        const index = nrPagina - 1;
        this.pdfService.togglePageStatus(index);
        const nouaStare = !!this.pdfService.pageStatuses()[index];
        if (nouaStare) {
          divPagina.classList.add('pagina-anulata');
        } else {
          divPagina.classList.remove('pagina-anulata');
        }
      };

    }
  }


}




}

