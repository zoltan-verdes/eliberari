import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Jurnal } from './jurnal/jurnal';
import { SafeResourceUrl } from '@angular/platform-browser';
import { RouterOutlet } from '@angular/router';
import { Nav } from './nav/nav';
import { PdfValidatorComponent } from "./app-pdf-validator/app-pdf-validator";



@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, Jurnal, Nav, RouterOutlet, PdfValidatorComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {

}
