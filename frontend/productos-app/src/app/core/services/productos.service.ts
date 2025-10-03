import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductoJsonApi, ProductoListResponseJsonApi } from '../models/producto-jsonapi.model';
import { environment } from '../../../environment';

@Injectable({ providedIn: 'root' })
export class ProductosService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  obtenerProductos(page: number = 0, size: number = 10): Observable<ProductoListResponseJsonApi> {
    const body = {
      pageable: { page, size, order: 'asc' }
    };
    return this.http.post<ProductoListResponseJsonApi>(this.apiUrl+"productos/filtro", body);
  }


  obtenerProductoPorId(id: string): Observable<ProductoListResponseJsonApi> {
    return this.http.get<ProductoListResponseJsonApi>(`${this.apiUrl+"productos"}/${id}`);
  }

  crearProducto(producto: Partial<ProductoJsonApi>): Observable<ProductoListResponseJsonApi> {
    const payload = {
      data: {
        attributes: producto
      }
    };
    return this.http.post<ProductoListResponseJsonApi>(this.apiUrl+"productos", payload);
  }

  actualizarProducto(id: string, producto: Partial<ProductoJsonApi>): Observable<ProductoListResponseJsonApi> {
    const payload = {
      data: {
        attributes: producto
      }
    };
    return this.http.put<ProductoListResponseJsonApi>(`${this.apiUrl + "productos"}/${id}`, payload);
  }
}
