import { AfterViewChecked, Component, ElementRef, Input, ViewChild } from '@angular/core';


@Component({
  selector: 'app-jurnal',
  imports: [],
  templateUrl: './jurnal.html',
  styleUrl: './jurnal.scss',
})
export class Jurnal implements AfterViewChecked {
   @Input() logs: any[] = [];

   // Referință către elementul de log pentru autoscroll
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;


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

