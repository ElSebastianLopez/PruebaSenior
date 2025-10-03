import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal-producto-detalle',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './modal-producto-detalle.component.html',
  styleUrls: ['./modal-producto-detalle.component.scss']
})
export class ModalProductoDetalleComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}
