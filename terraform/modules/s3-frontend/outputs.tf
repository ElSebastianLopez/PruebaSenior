output "bucket_name" {
  description = "Nombre del bucket"
  value       = aws_s3_bucket.frontend.id
}

output "bucket_arn" {
  description = "ARN del bucket"
  value       = aws_s3_bucket.frontend.arn
}

output "website_endpoint" {
  description = "Endpoint del website"
  value       = aws_s3_bucket_website_configuration.frontend.website_endpoint
}