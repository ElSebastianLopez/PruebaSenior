import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { TablaComponent } from '../../shared/components/tabla/tabla.component';
import { ProductoJsonApi } from '../../core/models/producto-jsonapi.model';
import { ProductosService } from '../../core/services/productos.service';
import { PageEvent } from '@angular/material/paginator';
import { ModalProductoDetalleComponent } from '../../shared/components/modal-producto-detalle/modal-producto-detalle.component';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ModalProductoFormComponent } from '../../shared/components/modal-producto-form/modal-producto-form.component';
import { BotonAccionComponent } from "../../shared/components/boton-accion/boton-accion.component";

@Component({
  selector: 'app-productos-listado',
  standalone: true,
  imports: [
    CommonModule,
    TablaComponent,
    MatDialogModule,
    ModalProductoDetalleComponent,
    ModalProductoFormComponent,
    BotonAccionComponent,
],
  templateUrl: './productos-listado.component.html',
  styleUrls: ['./productos-listado.component.scss'],
})
export class ProductosListadoComponent implements OnInit {
  productos: ProductoJsonApi[] = [];
  columnas: string[] = [
    'id',
    'nombre',
    'descripcion',
    'precio',
    'cantidad',
    'categoria',
    'acciones',
  ];
  datosTabla: any[] = [];
  pageSize = 10;
  pageIndex = 0;
  total = 0;

  constructor(
    private readonly productosService: ProductosService,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.cargarProductos(0, 10);
  }

  cargarProductos(page: number, size: number): void {
    this.productosService.obtenerProductos(page, size).subscribe({
      next: (res) => {
        this.total = res.meta.total;
        this.pageSize = res.meta.size;
        this.pageIndex = res.meta.page;

        this.datosTabla = res.data.map((p) => ({
          id: p.id,
          nombre: p.attributes.nombre,
          descripcion: p.attributes.descripcion,
          precio: p.attributes.precio,
          cantidad: p.attributes.cantidad,
          categoria: p.attributes.categoria,
        }));
      },
      error: (err) => {
        console.error('Error al cargar productos', err);
      },
    });
  }

  onPaginaCambiada(event: PageEvent): void {
    this.cargarProductos(event.pageIndex, event.pageSize);
  }

  abrirModalInfo(producto: ProductoJsonApi): void {
    this.productosService.obtenerProductoPorId(producto.id).subscribe({
      next: (res) => {
        this.dialog.open(ModalProductoDetalleComponent, {
          width: '600px',
          data: res.data[0],
        });
      },
      error: (err) => {
        console.error('Error al obtener detalle de producto', err);
      },
    });
  }

  abrirFormularioEdicion(producto: ProductoJsonApi): void {
    console.log('Producto a editar:', producto);
    const dialogRef = this.dialog.open(ModalProductoFormComponent, {
      width: '600px',
      data: {
        producto,
        modo: 'editar',
      },
    });

    dialogRef.afterClosed().subscribe((formValue) => {
      if (formValue) {
        this.productosService
          .actualizarProducto(producto.id, formValue.data.attributes)
          .subscribe({
            next: () => this.cargarProductos(this.pageIndex, this.pageSize),
            error: (err) => console.error('Error actualizando producto', err),
          });
      }
    });
  }

  abrirFormularioCreacion(): void {
    const dialogRef = this.dialog.open(ModalProductoFormComponent, {
      width: '600px',
      data: {
        modo: 'crear',
      },
    });

    dialogRef.afterClosed().subscribe((formValue) => {
      if (formValue) {
        this.productosService
          .crearProducto(formValue.data.attributes)
          .subscribe({
            next: () => this.cargarProductos(this.pageIndex, this.pageSize),
            error: (err) => console.error('Error Creando producto', err),
          });
      }
    });
  }

  onAccion(event: {
    tipo: 'detalle' | 'editar';
    producto: ProductoJsonApi;
  }): void {
    if (event.tipo === 'detalle') {
      this.abrirModalInfo(event.producto);
    } else if (event.tipo === 'editar') {
      this.abrirFormularioEdicion(event.producto);
    }
  }
}
