import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { ProductoJsonApi } from '../../../core/models/producto-jsonapi.model';

@Component({
  selector: 'app-modal-producto-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule],
  templateUrl: './modal-producto-form.component.html',
  styleUrls: ['./modal-producto-form.component.scss']
})
export class ModalProductoFormComponent {
  form: FormGroup;
  modo: 'crear' | 'editar';

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalProductoFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data?: { producto?: any, modo: 'crear' | 'editar' }
  ) {
    this.modo = data?.modo ?? 'crear';

    this.form = this.fb.group({
      nombre: [data?.producto?.nombre || '', Validators.required],
      descripcion: [data?.producto?.descripcion || '', Validators.required],
      precio: [data?.producto?.precio || 0, [Validators.required, Validators.min(0)]],
      categoria: [data?.producto?.categoria || '', Validators.required],
      cantidad: [data?.producto?.cantidad || 0, [Validators.required, Validators.min(0)]],
    });
  }

  guardar(): void {
    if (this.form.valid) {
      const payload = {
        data: {
          attributes: this.form.value
        }
      };
      this.dialogRef.close(payload);
    }
  }


  cerrar(): void {
    this.dialogRef.close();
  }
}
