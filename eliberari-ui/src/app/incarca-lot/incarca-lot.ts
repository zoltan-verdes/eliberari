import { HttpClient } from '@angular/common/http';
import { Component, inject, NgZone, signal, ViewChild } from '@angular/core';
import { ListLoturi } from './../list-loturi/list-loturi';
import { LogService } from '../log.service';

@Component({
  selector: 'app-incarca-lot',
  imports: [ListLoturi],
  templateUrl: './incarca-lot.html',
  styleUrl: './incarca-lot.scss',
})
export class IncarcaLot {
  private http = inject(HttpClient);
  private zone = inject(NgZone);
  private logService = inject(LogService);
  logs = this.logService.logs;
  isProcessing = signal(false);

  selectedFisLot = signal<File | null>(null);
  selectedFisScan = signal<File | null>(null);
  isUploading = signal(false);
  mesaj = signal<string | null>(null);
  // Adaugă noi semnale pentru starea PDF-ului
  selectedPdfFile = signal<File | null>(null);
  



  @ViewChild(ListLoturi) listLoturiComp!: ListLoturi;


  imagineCodBare = signal<string | null>(null);

  aduImaginea() {
    const eventSource = new EventSource('http://localhost:8080/api/ocr/stream-image');

    eventSource.addEventListener('image-data', (event: any) => {
      this.zone.run(() => {
        if (!event.data || event.data === 'null') {
          console.log('Imagine neidentificată');
          this.imagineCodBare.set(null);
        } else {
          // Salvăm string-ul base64 în signal
          this.imagineCodBare.set(`data:image/png;base64,${event.data}`);
        }
        eventSource.close(); // Închidem după ce am primit imaginea
      });
    });

    eventSource.onerror = () => {
      eventSource.close();
    };
  }

  onFisLotSelected(event: any) {
    const fisLot: File | undefined = event.target.files?.[0];
    const mime = fisLot?.type?.toLowerCase() || '';
    const name = fisLot?.name?.toLowerCase() || '';
    const isZip = mime.includes('zip') || name.endsWith('.zip');

    if (fisLot && isZip) {
      this.selectedFisLot.set(fisLot);
    } else {
      alert('Selectați un fișier zip valid. Tip fișier selectat: ' + (fisLot?.type || 'necunoscut'));
      this.selectedFisLot.set(null);
    }
  }

  uploadFile() {
    console.log('am intrat in upload');
    const fis = this.selectedFisLot;
    if (!fis()) { 
        console.error('Semnalul nu este inițializat sau fișierul lipsește');
        return; 
    }

// 1. Trimitem fișierul către semnalul ce va fi pasat componentei FIU
    this.selectedPdfFile.set(fis());

    this.isUploading.set(true);
    const formData = new FormData();
    formData.append('file', fis()!);
    
  
       // Presupunând că serverul returnează numele fișierului creat sau putem folosi fis.name
  

this.http.post<string[]>('http://localhost:8080/api/ocr/upload', formData).subscribe({
    next: (response) => {
        console.log('File uploaded:', response);
        this.logService.add('Fișier '+' încărcat: ' + fis()!.name);
        this.logService.addLogs(response);
        const [primul, ...loguri] =response;
        if (loguri.length > 0) {
          this.mesaj.set(loguri[loguri.length - 1]);
          setTimeout(() => this.mesaj.set(null), 5000);
        } 
       this.listLoturiComp.adauga(primul);


      this.isUploading.set(false);
        fis.set(null);
        // Reset the input
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (fileInput) fileInput.value = '';


/*
      // Presupunem că response.ignoredPages este un array de boolean
      this.pageStatuses.set(response.ignoredPages);
*/


    },
    error: (error) => {
      console.error('Upload failed:', error);
        this.logService.logs.update((l) => [...l, 'Eroare la încărcarea fișierului']);
      this.isUploading.set(false);
    }
  });
}  

tipareste(){
  console.log("tiparim");
}

}

export interface DateCompletateDTO {
  pagIgnorate: boolean[];
  log: string[];
}
