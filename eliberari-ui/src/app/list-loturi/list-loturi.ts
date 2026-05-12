import { Component, inject, OnInit, signal, Output, EventEmitter, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Rezultat } from '../rezultat/rezultat';
import { PdfService } from '../pdf.service';

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
  listeDisponibile = signal<string[]>([]);
  lotActiv = signal<string | null>(null);
  rezultate = signal<any[]>([]);

  @Output() lotSelectat = new EventEmitter<string>();

  ngOnInit() {
    this.incarcaListe();
  }

  adauga(nou: string) {
    this.listeDisponibile.update(liste => [...liste, nou]);
    this.lotActiv.set(nou);
    this.lotSelectat.emit(nou);
    this.pdfService.denumireLot.set(nou);
  }

  incarcaListe(isSelected?: string) {
    this.http.get<string[]>('http://localhost:8080/api/ocr/liste-disponibile').subscribe({
      next: (liste) => {
        this.listeDisponibile.set(liste);
        if (isSelected) {
          this.selecteazaLot(isSelected);
        }
      },
      error: (error) => console.error('Eroare la încărcarea listelor:', error)
    });
  }

  selecteazaLot(nume: string) {
    console.log("Ang: selectam lotul: "+nume);
    this.pdfService.denumireLot.set(nume);

    this.http.post<string[][]>(`http://localhost:8080/api/ocr/set-activ?nume=${nume}`, {}).subscribe({
      next: (response) => {
        this.rezultate.set(response)
        this.lotActiv.set(nume);
      },
      error: (err) => {
        if (err.status === 404) {
            alert('Lotul selectat nu a fost găsit. Te rugăm să selectezi unul din lista actualizată.');
            this.incarcaListe()
        } else {
        console.error('Nu s-a putut seta lotul activ', err);}
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
      ci: null,
      cim: null,
      cc: null
    });

    return [...dateReale, ...goale];
  });  
  
}
