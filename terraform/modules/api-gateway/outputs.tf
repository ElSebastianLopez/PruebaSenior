output "api_id" {
  description = "ID del API Gateway"
  value       = aws_api_gateway_rest_api.main.id
}

output "api_arn" {
  description = "ARN del API Gateway"
  value       = aws_api_gateway_rest_api.main.arn
}

output "deployment_id" {
  description = "ID del deployment"
  value       = aws_api_gateway_deployment.main.id
}