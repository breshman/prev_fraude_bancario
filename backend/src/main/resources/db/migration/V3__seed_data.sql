-- ====================================================================
-- Migración V3: Datos Semilla Iniciales
-- Catálogos de canales, tipos de transacción, roles, reglas RF001-RF008
-- y datos de prueba para ejecución con Postman
-- ====================================================================

-- 1. Canales
INSERT INTO canal (id_canal, codigo_canal, nombre_canal, es_activo) VALUES
(1, 'APP', 'Banca Móvil App', TRUE),
(2, 'WEB', 'Banca por Internet Web', TRUE),
(3, 'ATM', 'Cajero Automático', TRUE),
(4, 'POS', 'Terminal Punto de Venta', TRUE),
(5, 'VENTANILLA', 'Atención en Agencia', TRUE),
(6, 'AGENTE', 'Agente Bancario Corresponsal', TRUE),
(7, 'API', 'API Externa de Integración', TRUE)
ON CONFLICT (codigo_canal) DO NOTHING;

-- 2. Tipos de Transacción
INSERT INTO tipo_transaccion (id_tipo_transaccion, codigo_tipo, nombre_tipo, naturaleza, es_activo) VALUES
(1, 'TRANSFERENCIA', 'Transferencia Bancaria', 'DEBITO', TRUE),
(2, 'PAGO_SERVICIO', 'Pago de Servicios', 'DEBITO', TRUE),
(3, 'RETIRO', 'Retiro de Efectivo', 'DEBITO', TRUE),
(4, 'COMPRA', 'Compra con Tarjeta', 'DEBITO', TRUE),
(5, 'DEPOSITO', 'Depósito en Cuenta', 'CREDITO', TRUE)
ON CONFLICT (codigo_tipo) DO NOTHING;

-- 3. Roles del Sistema
INSERT INTO rol (id_rol, codigo_rol, nombre_rol, descripcion, es_activo) VALUES
(1, 'ADMIN', 'Administrador General', 'Control total y parametrización', TRUE),
(2, 'ANALISTA_FRAUDE', 'Analista de Fraude', 'Revisión de alertas y apertura de casos', TRUE),
(3, 'SUPERVISOR', 'Supervisor Antifraude', 'Supervisión y cierre de investigaciones', TRUE),
(4, 'AUDITOR', 'Auditor de Seguridad', 'Consulta y trazabilidad inmutable', TRUE)
ON CONFLICT (codigo_rol) DO NOTHING;

-- 4. Reglas Antifraude Predefinidas (Sección 18 de la Especificación Técnica)
INSERT INTO regla_fraude (id_regla, codigo_regla, nombre_regla, descripcion, nivel_riesgo, peso, version_regla, es_activa, parametros_json) VALUES
(1, 'RF001', 'Monto Inusual', 'Monto superior a 5 veces el promedio de los últimos 30 días', 'ALTO', 25.00, 1, TRUE, '{"multiplicador": 5.0, "dias": 30}'),
(2, 'RF002', 'Dispositivo Nuevo', 'Operación ejecutada desde un dispositivo no reconocido para el cliente', 'MEDIO', 15.00, 1, TRUE, '{}'),
(3, 'RF003', 'IP Nueva', 'Dirección IP de origen nunca antes vista en el historial del cliente', 'MEDIO', 10.00, 1, TRUE, '{}'),
(4, 'RF004', 'País Nuevo', 'Operación originada desde un país diferente al patrón de residencia habitual', 'ALTO', 30.00, 1, TRUE, '{"paisHabitual": "PE"}'),
(5, 'RF005', 'Horario Inusual', 'Operación realizada en madrugada (horario atípico entre 01:00 y 05:00 horas)', 'MEDIO', 10.00, 1, TRUE, '{"horaInicio": 1, "horaFin": 5}'),
(6, 'RF006', 'Alta Frecuencia', 'Más de 5 transacciones ejecutadas en la última hora por el mismo cliente', 'ALTO', 25.00, 1, TRUE, '{"limiteTransacciones": 5, "ventanaHoras": 1}'),
(7, 'RF007', 'Viaje Imposible', 'Velocidad de desplazamiento geográfico superior a 800 km/h respecto a la última ubicación', 'CRITICO', 40.00, 1, TRUE, '{"velocidadMaximaKmH": 800.0}'),
(8, 'RF008', 'Beneficiario Nuevo y Monto Alto', 'Transferencia hacia un beneficiario no conocido con monto mayor al umbral seguro', 'ALTO', 20.00, 1, TRUE, '{"umbralMonto": 5000.00}')
ON CONFLICT (codigo_regla) DO NOTHING;

-- 5. Usuario Inicial para Pruebas (analista_seguridad / Admin1234#)
-- Password encriptada con BCrypt: $2a$10$wOa8r0/d1q7vFmZ1e5P5Eeo4M5dZpYkYhD6QyG0HkE7C/r9Z6tKSu (Admin1234#)
INSERT INTO usuario_sistema (id_usuario, nombre_usuario, nombres, correo, hash_password, estado_usuario) VALUES
(1, 'analista_seguridad', 'Carlos Mendoza - Analista Senior', 'carlos.mendoza@banco.com', '$2a$10$wOa8r0/d1q7vFmZ1e5P5Eeo4M5dZpYkYhD6QyG0HkE7C/r9Z6tKSu', 'ACTIVO'),
(2, 'admin_fraude', 'Administradora de Seguridad', 'admin.seguridad@banco.com', '$2a$10$wOa8r0/d1q7vFmZ1e5P5Eeo4M5dZpYkYhD6QyG0HkE7C/r9Z6tKSu', 'ACTIVO')
ON CONFLICT (nombre_usuario) DO NOTHING;

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(1, 2), -- Carlos -> ANALISTA_FRAUDE
(2, 1)  -- Admin -> ADMIN
ON CONFLICT DO NOTHING;

-- 6. Clientes y Cuentas Semilla para Pruebas Postman
INSERT INTO cliente (id_cliente, tipo_documento, numero_documento, nombres, apellido_paterno, apellido_materno, correo_electronico, numero_telefono, codigo_pais_residencia, nivel_riesgo_cliente, es_activo) VALUES
(1, 'DNI', '45889900', 'Juan Alberto', 'Perez', 'Gomez', 'juan.perez@email.com', '987654321', 'PE', 'BAJO', TRUE),
(2, 'DNI', '78991122', 'Maria Elena', 'Rosas', 'Castro', 'maria.rosas@email.com', '912345678', 'PE', 'BAJO', TRUE)
ON CONFLICT (numero_documento) DO NOTHING;

INSERT INTO cuenta (id_cuenta, id_cliente, numero_cuenta, tipo_cuenta, codigo_moneda, saldo_disponible, estado_cuenta) VALUES
(1, 1, 'CTA-001-445522', 'AHORROS', 'PEN', 25000.00, 'ACTIVA'),
(2, 2, 'CTA-002-998811', 'AHORROS', 'PEN', 12000.00, 'ACTIVA')
ON CONFLICT (numero_cuenta) DO NOTHING;

INSERT INTO beneficiario (id_beneficiario, id_cliente, nombre_beneficiario, entidad_financiera, numero_cuenta_destino, codigo_pais, es_conocido, es_activo) VALUES
(1, 1, 'Maria Elena Rosas Castro', 'BCP', 'CTA-002-998811', 'PE', TRUE, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO dispositivo (id_dispositivo, id_cliente, huella_dispositivo, tipo_dispositivo, sistema_operativo, version_sistema, navegador, es_conocido) VALUES
(1, 1, 'device-fingerprint-habitual-01', 'MOVIL', 'Android', '14', 'Chrome Mobile', TRUE)
ON CONFLICT (huella_dispositivo) DO NOTHING;

-- Ajuste de secuencias para futuras inserciones
SELECT setval('cliente_id_cliente_seq', (SELECT COALESCE(MAX(id_cliente), 1) FROM cliente));
SELECT setval('cuenta_id_cuenta_seq', (SELECT COALESCE(MAX(id_cuenta), 1) FROM cuenta));
SELECT setval('beneficiario_id_beneficiario_seq', (SELECT COALESCE(MAX(id_beneficiario), 1) FROM beneficiario));
SELECT setval('dispositivo_id_dispositivo_seq', (SELECT COALESCE(MAX(id_dispositivo), 1) FROM dispositivo));
SELECT setval('usuario_sistema_id_usuario_seq', (SELECT COALESCE(MAX(id_usuario), 1) FROM usuario_sistema));
