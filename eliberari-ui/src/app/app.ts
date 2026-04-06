import { Component, inject, signal, NgZone, ViewChild, ElementRef, AfterViewChecked, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements AfterViewChecked {
  private http = inject(HttpClient);
  private zone = inject(NgZone);
  
  // Lista de mesaje care vor apărea în box
  logs = signal<string[]>([]);
  isProcessing = signal(false);
  rezultate = signal<any[]>([]);
 

  // Referință către elementul de log pentru autoscroll
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  pornesteProcesare() {
    this.isProcessing.set(true);
    this.logs.set(["Inițializare conexiune..."]);
    this.rezultate.set([]);
  
    const eventSource = new EventSource('http://localhost:8080/api/ocr/stream');
    
    eventSource.onmessage = (event) => {
      this.zone.run(() => {
        // event.data contine textul trimis de sseListener.onLog
        this.logs.update(l => [...l, event.data]);
      });
    };

    // Prindem evenimentul special pentru tabel
    eventSource.addEventListener('tabel', (event: any) => {
      this.zone.run(() => {
        this.rezultate.set(JSON.parse(event.data));
      });
    });




    eventSource.onerror = (error) => {
      this.zone.run(() => {
        console.log("Procesare finalizată sau întreruptă.");
        eventSource.close();
        this.isProcessing.set(false);
      });
    };

  }

rezultateCompletate = computed(() => {
  const dateReale = this.rezultate();
  const randuriGoaleNecesare = Math.max(0, 10 - dateReale.length);
  
  // Creăm un array cu restul de rânduri goale
  const goale = Array(randuriGoaleNecesare).fill({ 
    pagina: '', firma: '', cui: '', numar: '', data: '' 
  });
  
  return [...dateReale, ...goale];
});


  // Funcție pentru autoscroll la finalul listei
  ngAfterViewChecked() {        
    this.scrollToBottom();        
  } 

  scrollToBottom(): void {
    try {
        this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }                 
  }
}