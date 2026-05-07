import { Component, inject, OnInit, signal, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-list-loturi',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-loturi.html',
  styleUrl: './list-loturi.scss',
})
export class ListLoturi implements OnInit {
  private http = inject(HttpClient);
  listeDisponibile = signal<string[]>([]);
  lotActiv = signal<string | null>(null);

  @Output() lotSelectat = new EventEmitter<string>();

  ngOnInit() {
    this.incarcaListe();
  }

  adauga(nou: string) {
    this.listeDisponibile.update(liste => [...liste, nou]);
    this.lotActiv.set(nou);
    this.lotSelectat.emit(nou);
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
    this.http.post<string[]>(`http://localhost:8080/api/ocr/set-activ?nume=${nume}`, {}).subscribe({
      next: () => {
        this.lotActiv.set(nume);
        this.lotSelectat.emit(nume);
      },
      error: (err) => {
        if (err.status === 404) {
            this.listeDisponibile.set(err.error);
            alert('Lotul selectat nu a fost găsit. Te rugăm să selectezi unul din lista actualizată.');
        } else {
        console.error('Nu s-a putut seta lotul activ', err);}
      }
    });
  }
  
}
