
import { AfterViewChecked, Component, ElementRef, inject, ViewChild } from '@angular/core';
import { LogService } from '../log.service';


@Component({
  selector: 'app-jurnal',
  imports: [],
  templateUrl: './jurnal.html',
  styleUrl: './jurnal.scss',
})

export class Jurnal implements AfterViewChecked {
  
   private logService = inject(LogService);
   logs = this.logService.logs;

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

