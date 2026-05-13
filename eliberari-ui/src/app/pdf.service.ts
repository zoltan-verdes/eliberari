import { computed, effect, Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PdfService {
  doc = signal<File | null>(null);
  pageStatuses = signal<boolean[]>([]);
  rezultate = signal<any[]>([]);
  denumireLot = signal<string | "">("");

  
setFile(file: File | null) {
    console.log('PDF_SERVICE: setFile apelat cu:', file?.name);
    this.doc.set(file);
    console.log('PDF_SERVICE: Signal "doc" a fost actualizat.');
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
