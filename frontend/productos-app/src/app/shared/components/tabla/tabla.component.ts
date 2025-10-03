import { CommonModule } from '@angular/common';
import { Component, Input, ViewChild, AfterViewInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { BotonAccionComponent } from "../boton-accion/boton-accion.component";

@Component({
  selector: 'app-tabla',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, BotonAccionComponent],
  templateUrl: './tabla.component.html',
  styleUrls: ['./tabla.component.scss']
})
export class TablaComponent implements  OnChanges {
  @Input() columnas: string[] = [];
  @Input() datos: any[] = [];
  @Input() totalItems: number = 0;
  @Input() pageSize: number = 10;
  @Input() pageIndex: number = 0;

  @Output() paginaCambiada = new EventEmitter<PageEvent>();
  @Output() accion = new EventEmitter<any>();



  @ViewChild(MatPaginator) paginator!: MatPaginator;



  ngOnChanges(changes: SimpleChanges): void {
    if (changes['datos'] && changes['datos'].currentValue) {

    }
  }
}
