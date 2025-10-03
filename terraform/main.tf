# Módulo S3 Frontend
module "s3_frontend" {
  source = "./modules/s3-frontend"
  
  bucket_name   = "productos-app"
  frontend_path = var.frontend_path
}
# Módulo Secrets Manager
module "secrets" {
  source = "./modules/secrets"
  
  project_name = var.project_name
}

# Módulo API Gateway
module "api_gateway" {
  source = "./modules/api-gateway"
  
  project_name            = var.project_name
  productos_service_url   = var.productos_service_url
  inventario_service_url  = var.inventario_service_url
}

# CloudWatch Log Groups
resource "aws_cloudwatch_log_group" "productos" {
  name              = "/aws/productos"
  retention_in_days = 7
}

resource "aws_cloudwatch_log_group" "inventario" {
  name              = "/aws/inventario"
  retention_in_days = 7
}