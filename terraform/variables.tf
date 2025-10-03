variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "microservices"
}

variable "environment" {
  description = "Ambiente"
  type        = string
  default     = "dev"
}

variable "frontend_path" {
  description = "Path al build del frontend"
  type        = string
  default     = "../frontend/productos-app/dist/productos-app"
}

variable "productos_service_url" {
  description = "URL interna del servicio productos"
  type        = string
  default     = "http://productos:8081"
}

variable "inventario_service_url" {
  description = "URL interna del servicio inventario"
  type        = string
  default     = "http://inventario:8082"
}