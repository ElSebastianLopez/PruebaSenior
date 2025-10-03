resource "aws_api_gateway_rest_api" "main" {
  name        = "${var.project_name}-gateway"
  description = "API Gateway para microservicios"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

# Resource /productos
resource "aws_api_gateway_resource" "productos" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_rest_api.main.root_resource_id
  path_part   = "productos"
}

# Proxy resource /productos/{proxy+}
resource "aws_api_gateway_resource" "productos_proxy" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_resource.productos.id
  path_part   = "{proxy+}"
}

# Method ANY /productos
resource "aws_api_gateway_method" "productos_any" {
  rest_api_id   = aws_api_gateway_rest_api.main.id
  resource_id   = aws_api_gateway_resource.productos.id
  http_method   = "ANY"
  authorization = "NONE"
}

# Method ANY /productos/{proxy+}
resource "aws_api_gateway_method" "productos_proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.main.id
  resource_id   = aws_api_gateway_resource.productos_proxy.id
  http_method   = "ANY"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration /productos
resource "aws_api_gateway_integration" "productos" {
  rest_api_id             = aws_api_gateway_rest_api.main.id
  resource_id             = aws_api_gateway_resource.productos.id
  http_method             = aws_api_gateway_method.productos_any.http_method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "${var.productos_service_url}/dev/productos/api/v1/productos"
}

# Integration /productos/{proxy+}
resource "aws_api_gateway_integration" "productos_proxy" {
  rest_api_id             = aws_api_gateway_rest_api.main.id
  resource_id             = aws_api_gateway_resource.productos_proxy.id
  http_method             = aws_api_gateway_method.productos_proxy_any.http_method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "${var.productos_service_url}/dev/productos/api/v1/productos/{proxy}"

  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# Resource /inventario
resource "aws_api_gateway_resource" "inventario" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_rest_api.main.root_resource_id
  path_part   = "inventario"
}

# Proxy resource /inventario/{proxy+}
resource "aws_api_gateway_resource" "inventario_proxy" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_resource.inventario.id
  path_part   = "{proxy+}"
}

# Method ANY /inventario
resource "aws_api_gateway_method" "inventario_any" {
  rest_api_id   = aws_api_gateway_rest_api.main.id
  resource_id   = aws_api_gateway_resource.inventario.id
  http_method   = "ANY"
  authorization = "NONE"
}

# Method ANY /inventario/{proxy+}
resource "aws_api_gateway_method" "inventario_proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.main.id
  resource_id   = aws_api_gateway_resource.inventario_proxy.id
  http_method   = "ANY"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration /inventario
resource "aws_api_gateway_integration" "inventario" {
  rest_api_id             = aws_api_gateway_rest_api.main.id
  resource_id             = aws_api_gateway_resource.inventario.id
  http_method             = aws_api_gateway_method.inventario_any.http_method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "${var.inventario_service_url}/dev/inventario/api/v1/inventario"
}

# Integration /inventario/{proxy+}
resource "aws_api_gateway_integration" "inventario_proxy" {
  rest_api_id             = aws_api_gateway_rest_api.main.id
  resource_id             = aws_api_gateway_resource.inventario_proxy.id
  http_method             = aws_api_gateway_method.inventario_proxy_any.http_method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "${var.inventario_service_url}/dev/inventario/api/v1/inventario/{proxy}"

  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# Deployment
resource "aws_api_gateway_deployment" "main" {
  depends_on = [
    aws_api_gateway_integration.productos,
    aws_api_gateway_integration.productos_proxy,
    aws_api_gateway_integration.inventario,
    aws_api_gateway_integration.inventario_proxy
  ]
  rest_api_id = aws_api_gateway_rest_api.main.id
}

# Stage separado
resource "aws_api_gateway_stage" "prod" {
  deployment_id = aws_api_gateway_deployment.main.id
  rest_api_id   = aws_api_gateway_rest_api.main.id
  stage_name    = "prod"
}