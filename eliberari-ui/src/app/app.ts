import { Component, inject, signal, NgZone, computed, Signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Jurnal } from './jurnal/jurnal';
import { Rezultat } from './rezultat/rezultat';

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
    const fisLot = event.target.files[0];
    if (fisLot && fisLot.type === 'application/x-zip-compressed') {
      this.selectedFisLot.set(fisLot);
    } else {
      alert('Selectați un fișier zip valid. Tip fisier selectat: '+fisLot.type);
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
    this.isUploading.set(true);
    const formData = new FormData();
    formData.append('file', fis()!);


    this.http.post('http://localhost:8080/api/ocr/'+endpoint, formData, { 
  responseType: 'text'}).subscribe({
      next: (response) => {
        console.log('File uploaded:', response);
        this.logs.update((l) => [...l, 'Fișier '+endpoint+' încărcat: ' + fis()!.name]);
        this.isUploading.set(false);
        fis.set(null);
        // Reset the input
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
      },
      error: (error) => {
        console.error('Upload failed:', error);
        this.logs.update((l) => [...l, 'Eroare la încărcarea fișierului']);
        this.isUploading.set(false);
      }
    });
  }

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
