import { Component } from '@angular/core';

@Component({
  selector: 'app-grid-cell',
  standalone: true,
  template: `<ng-content></ng-content>`,
  styles: [`
    :host {
      padding: 5px;
      border-right: 1px solid red;
      border-bottom: 1px solid red;
      height: 20px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      display: flex;
      align-items: center;
    }
    :host:last-child {
      border-right: none;
    }
  `]
})
export class GridCell {}