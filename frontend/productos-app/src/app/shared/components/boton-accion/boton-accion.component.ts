import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-boton-accion',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './boton-accion.component.html',
  styleUrls: ['./boton-accion.component.scss']
})
export class BotonAccionComponent {
  @Input() icono: string = 'info';
  @Input() color: string = 'primary';
  @Input() tipo: 'icono' | 'texto' = 'icono';
  @Input() label?: string;
  @Output() accion = new EventEmitter<void>();
}
