import { Component, inject, OnInit, signal, Output, EventEmitter, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Rezultat } from '../rezultat/rezultat';
import { PdfService } from '../pdf.service';
import { CerereItem } from '../../model';
import { NotificationService } from '../notification.service';

@Component({
  selector: 'app-list-loturi',
  standalone: true,
  imports: [CommonModule, Rezultat],
  templateUrl: './list-loturi.html',
  styleUrl: './list-loturi.scss',
})
export class ListLoturi implements OnInit {
  private http = inject(HttpClient);
  private pdfService = inject(PdfService);
  listeDisponibile = signal<string[][]>([]);
  lotActiv = signal<string | null>(null);
  rezultate = signal<any[]>([]);
  isSplitting = signal(false);


  @Output() lotSelectat = new EventEmitter<string>();

  private notif = inject(NotificationService);

  ngOnInit() {
    this.incarcaListe();
  }

  adauga(nou: string) {
    this.incarcaListe(nou);
    this.lotActiv.set(nou);
    this.lotSelectat.emit(nou);
    this.pdfService.denumireLot.set(nou);
  }

  incarcaListe(isSelected?: string) {
    this.http.get<string[][]>('http://localhost:8080/api/ocr/liste-disponibile').subscribe({
      next: (liste) => {
        this.listeDisponibile.set(liste);
        if (isSelected) {
          this.selecteazaLot(isSelected);
        }
      },
      error: (error) => console.error('Eroare la încărcarea listelor:', error)
    });
  }

  
selecteazaLot(numeLot: string) {
  this.pdfService.denumireLot.set(numeLot);

  this.http.post<CerereItem[]>(`http://localhost:8080/api/ocr/set-activ?nume=${numeLot}`, {}).subscribe({
    next: (response) => {
      // Deoarece în Spring am returnat direct List<CerereSimpla>,
      // response este acum direct array-ul de care avem nevoie.
      this.rezultate.set(response);
      this.lotActiv.set(numeLot);
    },
    error: (err) => {
      if (err.status === 404) {
        alert('Lotul nu a mai fost găsit. Lista se va actualiza.');
        this.incarcaListe(); // Angular inițiază singur reîmprospătarea
      } else {
        console.error('Eroare severă:', err.error);
      }
    }
  });
}

separaPdfScanat() {
  const numeLot = this.pdfService.denumireLot();

  const payload = {
    numeLot: numeLot,
    statusChanged: this.pdfService.pageStatuses() // Presupunând că este un Signal care conține array-ul de booleeni
  };

    this.http.post(`http://localhost:8080/api/ocr/desparte`, payload, { responseType: 'text' } ).subscribe({
      next: (response) => {
        if (response === 'Separare terminat cu scces') {
          console.log('Separare finalizată:', response);
          this.notif.afiseaza(response, 'success');
        }
        else this.notif.afiseaza(response, 'error');
      },
      error: (err) => {
        if (err.status === 404) {
          this.notif.afiseaza('Lotul nu a mai fost găsit. Lista se va actualiza.','error');
          this.incarcaListe(); // Angular inițiază singur reîmprospătarea
        } else {
          console.error('Eroare severă:', err.error);
          this.notif.afiseaza(err.error, 'error');
        }
      }
    });
  }



  rezultateCompletate = computed(() => {
    const dateReale = this.rezultate();
    const randuriGoaleNecesare = Math.max(0, 20 - dateReale.length);

    // Creăm un array cu restul de rânduri goale
    const goale = Array(randuriGoaleNecesare).fill({
      numar: '',
      data: '',
      ci: null,
      cim: null,
      cc: null
    });

    return [...dateReale, ...goale];
  });  
  
}
