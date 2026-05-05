import { Component, inject, signal, NgZone, computed, Signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Jurnal } from './jurnal/jurnal';
import { Rezultat } from './rezultat/rezultat';
import { SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, Jurnal, Rezultat],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private http = inject(HttpClient);
  private zone = inject(NgZone);

  // Lista de mesaje care vor apărea în box
  logs = signal<string[]>([]);
  isProcessing = signal(false);
  rezultate = signal<any[]>([]);
  selectedFisLot = signal<File | null>(null);
  selectedFisScan = signal<File | null>(null);
  isUploading = signal(false);
  mesaj = signal<string | null>(null);
  // Adaugă noi semnale pentru starea PDF-ului
  selectedPdfFile = signal<File | null>(null);
  pageStatuses = signal<boolean[]>([]);


  pornesteProcesare() {
    this.isProcessing.set(true);
    this.logs.set(['Inițializare conexiune...']);
    this.rezultate.set([]);

    const eventSource = new EventSource('http://localhost:8080/api/ocr/stream');

    eventSource.onmessage = (event) => {
      this.zone.run(() => {
        // event.data contine textul trimis de sseListener.onLog
        this.logs.update((l) => [...l, event.data]);
      });
    };

    // Prindem evenimentul special pentru tabel
    eventSource.addEventListener('tabel', (event: any) => {
      this.zone.run(() => {
        this.rezultate.set(JSON.parse(event.data));
      });
    });

    //Prindem evenimentul de stream imagine

    eventSource.onerror = (error) => {
      this.zone.run(() => {
        console.log('Procesare finalizată sau întreruptă.');
        eventSource.close();
        this.isProcessing.set(false);
      });
    };
  }
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

  onFisScanSelected(event: any) {
    const fisScan = event.target.files[0];
    if (fisScan && fisScan.type === 'application/pdf') {
      this.selectedFisScan.set(fisScan);
    } else {
      alert('Selectați un fișier pdf valid. Tip fisier selectat: '+fisScan.type);
      this.selectedFisScan.set(null);
    }
  }



  uploadLot() {
    this.uploadFile(this.selectedFisLot, 'upload');
  }

  uploadScan() {
    this.uploadFile(this.selectedFisScan, 'upload-scan');
  }

  uploadFile(fis: WritableSignal<File | null>, endpoint:string) {
    console.log('am intrat in upload');
    if (!fis()) { 
        console.error('Semnalul nu este inițializat sau fișierul lipsește');
        return; 
    }

// 1. Trimitem fișierul către semnalul ce va fi pasat componentei FIU
    // Facem asta acum pentru ca utilizatorul să vadă PDF-ul în timp ce se încarcă (opțional)
    this.selectedPdfFile.set(fis());

    this.isUploading.set(true);
    const formData = new FormData();
    formData.append('file', fis()!);
  

    this.http.post('http://localhost:8080/api/ocr/'+endpoint, formData, { 
  responseType: 'text'}).subscribe({
    next: (response) => {
        console.log('File uploaded:', response);
        this.logs.update((l) => [...l, 'Fișier '+endpoint+' încărcat: ' + fis()!.name + ' - ' + response]);
        
        // Extragem ultimul string din răspuns pentru popup
        const linii = response.split('\n').filter((l: string) => l.trim());
        const ultimulMesaj = linii[linii.length - 1]?.trim();
        if (ultimulMesaj) {
          this.mesaj.set(ultimulMesaj);
          // Ștergem mesajul după 5 secunde
          setTimeout(() => this.mesaj.set(null), 5000);
        }
        
      this.isUploading.set(false);
        fis.set(null);
        // Reset the input
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (fileInput) fileInput.value = '';



/*
  // Recomandat: Schimbă responseType la 'json' dacă API-ul trimite array-ul de booleeni
  this.http.post<any>('http://localhost:8080/api/ocr/' + endpoint, formData).subscribe({
    next: (response) => {
      // Presupunem că response.ignoredPages este array-ul de boolean
      // Exemplu: [true, true, false, true]
      this.pageStatuses.set(response.ignoredPages);
      this.isUploading.set(false);

*/



    },
    error: (error) => {
      console.error('Upload failed:', error);
        this.logs.update((l) => [...l, 'Eroare la încărcarea fișierului']);
      this.isUploading.set(false);
    }
  });
}

handleStatusChange(newStatuses: boolean[]) {
    this.pageStatuses.set(newStatuses);
    console.log('Statusuri pagini actualizate manual:', newStatuses);
}
// Funcție pentru toggle manual
togglePageStatus(index: number) {
  this.pageStatuses.update(statuses => {
    const newStatuses = [...statuses];
    newStatuses[index] = !newStatuses[index];
    return newStatuses;
  });
}


// Metoda apelată de output-ul copilului
updateStatusesFromChild(newStatuses: boolean[]) {
  this.apiPageStatuses.set(newStatuses);
}

// În uploadFile, după ce primești răspunsul:
// this.apiPageStatuses.set(response.someArrayOfBooleans);
// this.selectedFile.set(fis()!);


  rezultateCompletate = computed(() => {
    const dateReale = this.rezultate();
    const randuriGoaleNecesare = Math.max(0, 10 - dateReale.length);

    // Creăm un array cu restul de rânduri goale
    const goale = Array(randuriGoaleNecesare).fill({
      numar: '',
      data: '',
      firma: '',
      cui: '',
      incheiere: null,
      ci: null,
      cim: null,
      cc: null
    });

    return [...dateReale, ...goale];
  });
}
