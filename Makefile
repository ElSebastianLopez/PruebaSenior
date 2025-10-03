.PHONY: deploy destroy plan init logs clean build-frontend

deploy: clean build-frontend init
	@echo "Levantando servicios Docker..."
	docker-compose up -d --build
	@echo "Esperando LocalStack (40s)..."
	@sleep 40
	@echo "Desplegando infraestructura con Terraform..."
	cd terraform && terraform apply -auto-approve
	@echo ""
	@echo "========================================"
	@echo "DESPLIEGUE COMPLETO"
	@echo "========================================"
	@$(MAKE) show-urls
	@echo "========================================"
	@$(MAKE) open-browser

build-frontend:
	@echo "Compilando frontend Angular..."
	cd frontend/productos-app && npm install && npm run build  -- --base-href=/productos-app/

init:
	@echo "Inicializando Terraform..."
	cd terraform && terraform init

plan:
	cd terraform && terraform plan

show-urls:
	@echo ""
	@echo "FRONTEND:"
	@cd terraform && echo "http://localhost:4566/$$(terraform output -raw bucket_name)/index.html"
	@echo ""
	@echo "API GATEWAY:"
	@cd terraform && terraform output -raw api_gateway_url
	@echo ""
	@echo "API PRODUCTOS:"
	@cd terraform && terraform output -raw api_productos_url
	@echo ""
	@echo "API INVENTARIO:"
	@cd terraform && terraform output -raw api_inventario_url
	@echo ""

open-browser:
	@echo "Abriendo navegador..."
	@sleep 2
	@FRONTEND_URL=$$(cd terraform && terraform output -raw frontend_url 2>/dev/null); \
	if [ -n "$$FRONTEND_URL" ]; then \
		xdg-open "$$FRONTEND_URL" 2>/dev/null || open "$$FRONTEND_URL" 2>/dev/null || echo "Abrir manualmente: $$FRONTEND_URL"; \
	fi

destroy:
	cd terraform && terraform destroy -auto-approve
	docker-compose down -v

clean:
	docker stop $$(docker ps -q --filter ancestor=localstack/localstack) 2>/dev/null || true
	rm -rf terraform/.terraform
	rm -rf terraform/.terraform.lock.hcl
	rm -rf .localstack
	docker-compose down -v 2>/dev/null || true

logs:
	docker-compose logs -f