import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { ListLoturi } from './../list-loturi/list-loturi';
import { LogService } from '../log.service';
import { NotificationService } from '../notification.service';

@Component({
  selector: 'app-incarca-lot',
  imports: [ListLoturi],
  templateUrl: './incarca-lot.html',
  styleUrl: './incarca-lot.scss',
})
export class IncarcaLot {
  private http = inject(HttpClient);
  private logService = inject(LogService);
  logs = this.logService.logs;
  isProcessing = signal(false);
  private notif = inject(NotificationService);

  selectedFisLot = signal<File | null>(null);
  isUploading = signal(false);
  isPrinting = signal(false);
  //mesaj = signal<string | null>(null);
  // Adaugă noi semnale pentru starea PDF-ului
  selectedPdfFile = signal<File | null>(null);


  //@ViewChild(ListLoturi) listLoturiComp!: ListLoturi;
  listLoturiComp = viewChild(ListLoturi);
  selectedLot = computed(() => this.listLoturiComp()?.lotActiv());


  onFisLotSelected(event: any) {
    const fisLot: File | undefined = event.target.files?.[0];
    const mime = fisLot?.type?.toLowerCase() || '';
    const name = fisLot?.name?.toLowerCase() || '';
    const isZip = mime.includes('zip') || name.endsWith('.zip');

    if (fisLot && isZip) {
      this.selectedFisLot.set(fisLot);
      this.uploadFile();
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
  

this.http.post<string[]>('/api/ocr/upload', formData).subscribe({
    next: (response) => {
        console.log('File uploaded:', response);
        this.logService.add('Fișier '+' încărcat: ' + fis()!.name);
        this.logService.addLogs(response);
        const [denumireLot, ...loguri] =response;
        if (loguri.length > 0) {
          this.notif.afiseaza(loguri[loguri.length - 1],'success');
        } 
       this.listLoturiComp()?.adauga(denumireLot);
       this.listLoturiComp()?.selecteazaLot(denumireLot);


      this.isUploading.set(false);
        fis.set(null);
        // Reset the input
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (fileInput) fileInput.value = '';

    },
    error: (error) => {
      console.error('Upload failed:', error);
        this.logService.logs.update((l) => [...l, 'Eroare la încărcarea fișierului']);
      this.isUploading.set(false);
    }
  });
}  

tipareste(){
  console.log("tiparim lot zip "+this.selectedLot());
  this.isPrinting.set(true);
  this.http.post(`/api/ocr/tipareste-lot-dir?nume=${this.selectedLot()}`, {}, {responseType: 'text'}).subscribe({
    next: (response) => {
      console.log('Operatie de tiparire a lotului ' + this.selectedLot() + ' s-a finalizat cu succes:');
      this.notif.afiseaza('Lotul '+this.selectedLot()+' a fost tipărit cu succes!','success');
      this.isPrinting.set(false);
    },
    error: (err) => {
        alert('Au fost intampinate probeleme la listarea lotului '+this.selectedLot());
        console.error('Eroare severă:', err.error);
    }
  });
}

}

export interface DateCompletateDTO {
  pagIgnorate: boolean[];
  log: string[];
}
