import { Component, Input } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-rezultat',
  imports: [],
  templateUrl: './rezultat.html',
  styleUrl: './rezultat.scss',
})

export class Rezultat {
  @Input() date: any[] = [];

  formatStatus(status: any): string {
    if (!status || (status.nrPagLot === 0 && status.nrPagScanat === 0)) return '- / -';
    if (!status || (status.nrPagLot === 0)) return '- / '+  status.nrPagScanat;
    if (!status || (status.nrPagScanat === 0)) return status.nrPagLot + ' / -';

    return `${status.nrPagLot} / ${status.nrPagScanat}`;
  }

  isComplet(status: any): boolean {
    return status?.complet ?? true;
  }
}
