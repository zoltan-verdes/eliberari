import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  // Semnalul centralizat pentru log-uri
  logs = signal<string[]>([]);

  addLog(message: string) {
    this.logs.update(prev => [...prev, message]);
  }
}
