import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  // Semnalul centralizat pentru log-uri
  logs = signal<string[]>([]);

  add(message: string) {
    this.logs.update(prev => [...prev, message]);
  }
  addLogs(messages: string[]) {
    this.logs.update(prev => [...prev, ...messages]);
  }
}
