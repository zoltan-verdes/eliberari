import { Component, HostBinding, Input } from '@angular/core';

@Component({
  selector: 'app-grid-row',
  standalone: true,
  template: `<ng-content></ng-content>`,
  styles: [`
    :host {
      display: contents; /* Esențial: rândul "dispare" vizual, lăsând celulele să se alinieze la grid-ul părintelui */
    }
    /* Stiluri aplicate celulelor din acest rând */
    :host-context(app-grid-row) ::ng-content(app-grid-cell) {
       border-bottom: 1px solid red;
    }
  `]
})
export class GridRow {
  @Input() isHeader: boolean = false;

  @HostBinding('style.font-weight') get fontWeight() {
    return this.isHeader ? 'bold' : 'normal';
  }
  
  @HostBinding('style.background-color') get bgColor() {
    return this.isHeader ? '#f1f1f1' : 'transparent';
  }
}