import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'info';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  // Semnal care ține textul mesajului curent
  mesaj = signal<string | null>(null);
  // Semnal pentru stilul popup-ului
  tip = signal<NotificationType>('success');

  private timeoutId: any = null;

  afiseaza(text: string, tip: NotificationType = 'success') {
    // Dacă exista deja o notificare activă, curățăm timeout-ul vechi
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }

    this.mesaj.set(text);
    this.tip.set(tip);

    // Închidem automat popup-ul după 3 secunde
    this.timeoutId = setTimeout(() => {
      this.mesaj.set(null);
    }, 3000);
  }
}