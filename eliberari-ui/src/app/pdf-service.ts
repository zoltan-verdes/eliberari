import { computed, Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PdfService {
  // Semnalul centralizat pentru log-uri
  
  doc = signal<File | null>(null);
  pageStatuses = signal<boolean[]>([]);
  rezultate = signal<any[]>([]);
  denumireLot = signal<string | null>;

  
  setFile(file: File | null) {
    this.doc.set(file);
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
  this.pageStatuses.set(newStatuses);
}

// În uploadFile, după ce primești răspunsul:
// this.apiPageStatuses.set(response.someArrayOfBooleans);
// this.selectedFile.set(fis()!);

}
