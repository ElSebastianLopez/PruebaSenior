CREATE SCHEMA IF NOT EXISTS dbo;

CREATE TABLE IF NOT EXISTS dbo.productos (
    id BIGSERIAL PRIMARY KEY,
    actualizado_en TIMESTAMP(6),
    categoria VARCHAR(50),
    creado_en TIMESTAMP(6),
    descripcion VARCHAR(255),
    nombre VARCHAR(100) NOT NULL,
    precio NUMERIC(10, 2) NOT NULL
);

INSERT INTO dbo.productos (actualizado_en, categoria, creado_en, descripcion, nombre, precio) VALUES
(NOW(), 'Tecnología', NOW(), 'Laptop HP Pavilion 15 pulgadas', 'Laptop HP', 799.99),
(NOW(), 'Tecnología', NOW(), 'Monitor Dell 27 pulgadas 4K', 'Monitor Dell', 349.99),
(NOW(), 'Tecnología', NOW(), 'Teclado mecánico RGB', 'Teclado Logitech', 59.99);

CREATE TABLE IF NOT EXISTS dbo.inventarios (
    producto_id BIGINT PRIMARY KEY,
    cantidad_disponible INTEGER NOT NULL,
    ultima_actualizacion TIMESTAMP(6) NOT NULL
);

INSERT INTO dbo.inventarios (producto_id, cantidad_disponible, ultima_actualizacion) VALUES
(1, 15, NOW()),
(2, 27, NOW()),
(3, 42, NOW());
