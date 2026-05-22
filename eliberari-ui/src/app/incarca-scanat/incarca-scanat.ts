import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, NgZone, signal, ViewChild, WritableSignal } from '@angular/core';
import { ListLoturi } from './../list-loturi/list-loturi';
import { LogService } from '../log.service';
import { PdfService } from '../pdf.service';
import { NotificationService } from '../notification.service';

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
  private notif = inject(NotificationService);

  logs = this.logService.logs;
  isProcessing = signal(false);
  selectedFisScan = signal<File | null>(null);
  isUploading = signal(false);
  mesaj = signal<string | null>(null);
  isSplitting = signal(false);
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
        
this.http.post<boolean[]>('/api/ocr/upload-scan', formData).subscribe({
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
      this.pdfService.setFile(fisier);
      this.pdfService.pageStatuses.set(response);

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

  afiseazaPdfLocal() {
  const fisier = this.selectedFisScan();
  if (fisier) {
    console.log('Trimit fișierul local către service pentru afișare:', fisier.name);
    
    // Resetăm statusurile la un array gol (pentru a fi inițializate de validator)
    this.pdfService.pageStatuses.set([]);
    
    // Setăm fișierul în service - asta ar trebui să fie suficient
    this.pdfService.setFile(fisier);
    
    this.logService.add('Previzualizare locală activată pentru: ' + fisier.name);
  } else {
    alert('Te rugăm să selectezi un fișier PDF mai întâi.');
  }
}


separaPdfScanat() {
  this.isSplitting.set(true);
  const numeLot = this.pdfService.denumireLot();

  const payload = {
    numeLot: numeLot,
    statusChanged: this.pdfService.pageStatuses() // Presupunând că este un Signal care conține array-ul de booleeni
  };

    this.http.post(`/api/ocr/desparte`, payload, { responseType: 'text' } ).subscribe({
      next: (response) => {
        if (response === 'Separare terminat cu succes') {
          console.log('Separare finalizată:', response);
          this.notif.afiseaza(response, 'success');
        }
        else this.notif.afiseaza(response, 'error');
        this.isSplitting.set(false);        
      },
      error: (err) => {
        if (err.status === 404) {
          this.notif.afiseaza('Lotul nu a mai fost găsit. Lista se va actualiza.','error');
          this.listLoturiComp.incarcaListe(); // Angular inițiază singur reîmprospătarea
        } else {
          console.error('Eroare severă:', err.error);
          this.notif.afiseaza(err.error, 'error');
        }
        this.isSplitting.set(false);
      }
    });
  }

}

export interface DateCompletateDTO {
  pagIgnorate: boolean[];
  log: string[];
}
