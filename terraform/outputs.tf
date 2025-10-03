output "frontend_url" {
  description = "URL del frontend en S3"
  value       = "http://localhost:4566/${module.s3_frontend.bucket_name}/index.html"
}

output "api_gateway_url" {
  description = "URL base del API Gateway"
  value       = "http://localhost:4566/restapis/${module.api_gateway.api_id}/prod/_user_request_"
}

output "api_productos_url" {
  description = "URL completa API Productos"
  value       = "http://localhost:4566/restapis/${module.api_gateway.api_id}/prod/_user_request_/productos"
}

output "api_inventario_url" {
  description = "URL completa API Inventario"
  value       = "http://localhost:4566/restapis/${module.api_gateway.api_id}/prod/_user_request_/inventario"
}

output "bucket_name" {
  description = "Nombre del bucket S3"
  value       = module.s3_frontend.bucket_name
}

output "api_gateway_id" {
  description = "ID del API Gateway"
  value       = module.api_gateway.api_id
}

output "db_secret_arn" {
  description = "ARN del secreto de DB"
  value       = module.secrets.db_secret_arn
}