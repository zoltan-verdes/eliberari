import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-grid-table',
  standalone: true,
  template: `
    <div class="grid-table-container" [style.max-height]="maxHeight">
      <div class="grid-table" [style.grid-template-columns]="columns">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [`
    .grid-table-container {
      overflow: auto;
      border: 1px solid red;
    }
    .grid-table {
      display: grid;
      width: 100%;
    }
  `]
})
export class GridTable {
  @Input() columns: string = 'repeat(8, 1fr)';
  @Input() maxHeight: string = '250px';
}