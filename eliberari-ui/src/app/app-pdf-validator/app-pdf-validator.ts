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
  private pdfService = inject(PdfService);
  
  // Sursă reactivă care se curăță automat
  localPdfSrc = computed(() => {
    const file = this.pdfService.doc();
    return file instanceof File ? URL.createObjectURL(file) : null;
  });
}