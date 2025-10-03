export interface ProductoJsonApi {
  type: string;
  id: string;
  attributes: {
    nombre: string;
    descripcion: string;
    precio: number;
    categoria: string;
    cantidad: number | null;
    creadoEn: string;
    actualizadoEn: string;
  };
}

export interface MetaJsonApi {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  order: string;
}

export interface ProductoListResponseJsonApi {
  data: ProductoJsonApi[];
  meta: MetaJsonApi;
  links: { [key: string]: string };
}
