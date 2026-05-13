import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, NgZone, signal, ViewChild, WritableSignal } from '@angular/core';
import { ListLoturi } from './../list-loturi/list-loturi';
import { LogService } from '../log.service';
import { PdfService } from '../pdf.service';

@Component({
  selector: 'app-incarca-lot',
  imports: [ListLoturi],
  templateUrl: './incarca-scanat.html',
  styleUrl: './incarca-scanat.scss',
})
export class IncarcaScanat {
  private http = inject(HttpClient);
  private zone = inject(NgZone);
  private logService = inject(LogService);
  private pdfService = inject(PdfService);
  logs = this.logService.logs;
  isProcessing = signal(false);
  selectedFisScan = signal<File | null>(null);
  isUploading = signal(false);
  mesaj = signal<string | null>(null);
  // Adaugă noi semnale pentru starea PDF-ului
  

  @ViewChild(ListLoturi) listLoturiComp!: ListLoturi;


  onFisScanSelected(event: any) {
    const fisScan = event.target.files[0];
    if (fisScan && fisScan.type === 'application/pdf') {
      this.selectedFisScan.set(fisScan);
    } else {
      alert('Selectați un fișier pdf valid. Tip fisier selectat: '+fisScan.type);
      this.selectedFisScan.set(null);
    }
  }
  
  uploadScan() {
   console.log('am intrat in upload');
    if (!this.selectedFisScan()) { 
        console.error('Semnalul nu este inițializat sau fișierul lipsește');
        return; 
    }

    const fisier = this.selectedFisScan()!;
    
    this.isUploading.set(true);
    const formData = new FormData();
    formData.append('file', fisier, this.pdfService.denumireLot()+'.pdf');
        
this.http.post<boolean[]>('http://localhost:8080/api/ocr/upload-scan', formData).subscribe({
    next: (response) => {
        console.log('File uploaded:', response);
        this.logService.add('Fișier încărcat: ' + fisier.name);
/*        this.logService.addLogs(response.log);
        const ultimul = response.log[response.log.length - 1];
        
        if (ultimul) {
          this.mesaj.set(ultimul);
          setTimeout(() => this.mesaj.set(null), 5000);
        } 
*/
        this.pdfService.pageStatuses.set(response);
      this.pdfService.setFile(fisier);

      this.isUploading.set(false);
      this.selectedFisScan.set(null);
      // Reset the input
      const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
      if (fileInput) fileInput.value = '';

    },
    error: (error) => {
      console.error('Upload failed:', error);
        this.logService.logs.update((l) => [...l, 'Eroare la încărcarea fișierului']);
      this.isUploading.set(false);
    }
  });  }

  redenumeste (){
    ;
  }

}

export interface DateCompletateDTO {
  pagIgnorate: boolean[];
  log: string[];
}
