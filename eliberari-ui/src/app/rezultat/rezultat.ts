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

}







