-- ====================================================================
-- Migración V2: Definición completa de las 18 tablas del sistema antifraude
-- Basado en la especificación técnica y diccionario de datos v1.1
-- ====================================================================

-- 1. Limpieza de tablas preliminares si existen
DROP TABLE IF EXISTS alerta_fraude CASCADE;
DROP TABLE IF EXISTS transaccion CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;

-- ====================================================================
-- 2. DOMINIO MAESTROS
-- ====================================================================

-- Tabla 9.1: cliente
CREATE TABLE cliente (
    id_cliente BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100),
    apellido_materno VARCHAR(100),
    fecha_nacimiento DATE,
    correo_electronico VARCHAR(150),
    numero_telefono VARCHAR(30),
    codigo_pais_residencia CHAR(2),
    nivel_riesgo_cliente VARCHAR(20) NOT NULL DEFAULT 'BAJO',
    es_activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ
);

-- Tabla 9.2: cuenta
CREATE TABLE cuenta (
    id_cuenta BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL REFERENCES cliente(id_cliente),
    numero_cuenta VARCHAR(34) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(30) NOT NULL,
    codigo_moneda CHAR(3) NOT NULL,
    saldo_disponible NUMERIC(18,2) NOT NULL DEFAULT 0.00,
    estado_cuenta VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_cierre DATE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla 9.3: beneficiario
CREATE TABLE beneficiario (
    id_beneficiario BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL REFERENCES cliente(id_cliente),
    nombre_beneficiario VARCHAR(150) NOT NULL,
    entidad_financiera VARCHAR(120),
    numero_cuenta_destino VARCHAR(50),
    codigo_pais CHAR(2),
    es_conocido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_primer_uso TIMESTAMPTZ,
    fecha_ultimo_uso TIMESTAMPTZ,
    es_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla 9.4: canal
CREATE TABLE canal (
    id_canal SMALLSERIAL PRIMARY KEY,
    codigo_canal VARCHAR(20) NOT NULL UNIQUE,
    nombre_canal VARCHAR(100) NOT NULL,
    es_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla 9.5: tipo_transaccion
CREATE TABLE tipo_transaccion (
    id_tipo_transaccion SMALLSERIAL PRIMARY KEY,
    codigo_tipo VARCHAR(40) NOT NULL UNIQUE,
    nombre_tipo VARCHAR(120) NOT NULL,
    naturaleza VARCHAR(10) NOT NULL,
    es_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla 9.6: dispositivo
CREATE TABLE dispositivo (
    id_dispositivo BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL REFERENCES cliente(id_cliente),
    huella_dispositivo VARCHAR(255) UNIQUE,
    tipo_dispositivo VARCHAR(30),
    sistema_operativo VARCHAR(50),
    version_sistema VARCHAR(30),
    navegador VARCHAR(50),
    es_conocido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_primer_uso TIMESTAMPTZ,
    fecha_ultimo_uso TIMESTAMPTZ
);

-- ====================================================================
-- 3. DOMINIO SEGURIDAD Y GOBIERNO (Requerido antes de Alertas/Casos)
-- ====================================================================

-- Tabla 9.15: usuario_sistema
CREATE TABLE usuario_sistema (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
    nombres VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    hash_password VARCHAR(255) NOT NULL,
    estado_usuario VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    ultimo_acceso TIMESTAMPTZ,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla 9.16: rol
CREATE TABLE rol (
    id_rol SMALLSERIAL PRIMARY KEY,
    codigo_rol VARCHAR(40) NOT NULL UNIQUE,
    nombre_rol VARCHAR(100) NOT NULL,
    descripcion VARCHAR(250),
    es_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla 9.17: usuario_rol
CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario_sistema(id_usuario) ON DELETE CASCADE,
    id_rol SMALLINT NOT NULL REFERENCES rol(id_rol) ON DELETE CASCADE,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_rol)
);

-- ====================================================================
-- 4. DOMINIO TRANSACCIONAL
-- ====================================================================

-- Tabla 9.7: transaccion
CREATE TABLE transaccion (
    id_transaccion BIGSERIAL PRIMARY KEY,
    codigo_transaccion UUID NOT NULL UNIQUE,
    id_cuenta BIGINT NOT NULL REFERENCES cuenta(id_cuenta),
    id_beneficiario BIGINT REFERENCES beneficiario(id_beneficiario),
    id_tipo_transaccion SMALLINT NOT NULL REFERENCES tipo_transaccion(id_tipo_transaccion),
    id_canal SMALLINT NOT NULL REFERENCES canal(id_canal),
    id_dispositivo BIGINT REFERENCES dispositivo(id_dispositivo),
    monto_transaccion NUMERIC(18,2) NOT NULL CHECK (monto_transaccion > 0),
    codigo_moneda CHAR(3) NOT NULL,
    fecha_hora_transaccion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    saldo_anterior NUMERIC(18,2),
    saldo_posterior NUMERIC(18,2),
    direccion_ip VARCHAR(45),
    codigo_pais CHAR(2),
    ciudad VARCHAR(100),
    latitud NUMERIC(9,6),
    longitud NUMERIC(9,6),
    estado_transaccion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- 5. DOMINIO ANTIFRAUDE Y SCORING
-- ====================================================================

-- Tabla 9.8: caracteristica_transaccion
CREATE TABLE caracteristica_transaccion (
    id_transaccion BIGINT PRIMARY KEY REFERENCES transaccion(id_transaccion) ON DELETE CASCADE,
    monto_promedio_30d NUMERIC(18,2),
    monto_maximo_30d NUMERIC(18,2),
    cantidad_transacciones_10m INTEGER NOT NULL DEFAULT 0,
    cantidad_transacciones_1h INTEGER NOT NULL DEFAULT 0,
    cantidad_transacciones_24h INTEGER NOT NULL DEFAULT 0,
    cantidad_destinatarios_24h INTEGER NOT NULL DEFAULT 0,
    minutos_desde_ultima_transaccion INTEGER,
    distancia_ultima_transaccion_km NUMERIC(10,2),
    es_dispositivo_nuevo BOOLEAN NOT NULL DEFAULT FALSE,
    es_ip_nueva BOOLEAN NOT NULL DEFAULT FALSE,
    es_pais_nuevo BOOLEAN NOT NULL DEFAULT FALSE,
    es_ciudad_nueva BOOLEAN NOT NULL DEFAULT FALSE,
    es_beneficiario_nuevo BOOLEAN NOT NULL DEFAULT FALSE,
    es_horario_inusual BOOLEAN NOT NULL DEFAULT FALSE,
    es_monto_inusual BOOLEAN NOT NULL DEFAULT FALSE,
    es_velocidad_inusual BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_calculo TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla 9.9: regla_fraude
CREATE TABLE regla_fraude (
    id_regla SERIAL PRIMARY KEY,
    codigo_regla VARCHAR(30) NOT NULL UNIQUE,
    nombre_regla VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    nivel_riesgo VARCHAR(20) NOT NULL,
    peso NUMERIC(5,2) NOT NULL,
    parametros_json JSONB,
    version_regla INTEGER NOT NULL DEFAULT 1,
    es_activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio_vigencia TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin_vigencia TIMESTAMPTZ
);

-- Tabla 9.10: evaluacion_fraude
CREATE TABLE evaluacion_fraude (
    id_evaluacion BIGSERIAL PRIMARY KEY,
    id_transaccion BIGINT NOT NULL REFERENCES transaccion(id_transaccion),
    fraude_score NUMERIC(5,2) NOT NULL CHECK (fraude_score >= 0 AND fraude_score <= 100),
    nivel_riesgo VARCHAR(20) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    es_posible_fraude BOOLEAN NOT NULL DEFAULT FALSE,
    modelo_utilizado VARCHAR(100) DEFAULT 'REGLAS',
    version_modelo VARCHAR(40) DEFAULT '1.0',
    fecha_evaluacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duracion_ms INTEGER
);

-- Tabla 9.11: evaluacion_regla
CREATE TABLE evaluacion_regla (
    id_evaluacion_regla BIGSERIAL PRIMARY KEY,
    id_evaluacion BIGINT NOT NULL REFERENCES evaluacion_fraude(id_evaluacion) ON DELETE CASCADE,
    id_regla INTEGER NOT NULL REFERENCES regla_fraude(id_regla),
    se_disparo BOOLEAN NOT NULL,
    valor_detectado VARCHAR(250),
    peso_aplicado NUMERIC(5,2) NOT NULL,
    detalle_json JSONB
);

-- Tabla 9.12: alerta_fraude
CREATE TABLE alerta_fraude (
    id_alerta BIGSERIAL PRIMARY KEY,
    codigo_alerta UUID NOT NULL UNIQUE,
    id_transaccion BIGINT NOT NULL REFERENCES transaccion(id_transaccion),
    id_evaluacion BIGINT NOT NULL REFERENCES evaluacion_fraude(id_evaluacion),
    tipo_alerta VARCHAR(50) NOT NULL,
    nivel_prioridad VARCHAR(20) NOT NULL,
    descripcion VARCHAR(500),
    estado_alerta VARCHAR(30) NOT NULL DEFAULT 'GENERADA',
    id_usuario_asignado BIGINT REFERENCES usuario_sistema(id_usuario),
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revision TIMESTAMPTZ
);

-- Tabla 9.13: caso_fraude
CREATE TABLE caso_fraude (
    id_caso BIGSERIAL PRIMARY KEY,
    codigo_caso UUID NOT NULL UNIQUE,
    id_alerta BIGINT NOT NULL UNIQUE REFERENCES alerta_fraude(id_alerta),
    id_usuario_responsable BIGINT REFERENCES usuario_sistema(id_usuario),
    estado_caso VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',
    resultado_caso VARCHAR(30),
    comentario_cierre TEXT,
    fecha_apertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMPTZ
);

-- Tabla 9.14: historial_caso
CREATE TABLE historial_caso (
    id_historial BIGSERIAL PRIMARY KEY,
    id_caso BIGINT NOT NULL REFERENCES caso_fraude(id_caso) ON DELETE CASCADE,
    id_usuario BIGINT REFERENCES usuario_sistema(id_usuario),
    tipo_evento VARCHAR(40) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    comentario TEXT,
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla 9.18: auditoria
CREATE TABLE auditoria (
    id_auditoria BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT REFERENCES usuario_sistema(id_usuario),
    entidad VARCHAR(80) NOT NULL,
    id_entidad VARCHAR(80) NOT NULL,
    accion VARCHAR(40) NOT NULL,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    direccion_ip VARCHAR(45),
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- 6. ÍNDICES DE RENDIMIENTO RECOMENDADOS
-- ====================================================================
CREATE INDEX idx_transaccion_cuenta_fecha ON transaccion(id_cuenta, fecha_hora_transaccion DESC);
CREATE INDEX idx_transaccion_codigo ON transaccion(codigo_transaccion);
CREATE INDEX idx_evaluacion_transaccion ON evaluacion_fraude(id_transaccion);
CREATE INDEX idx_alerta_estado_prioridad ON alerta_fraude(estado_alerta, nivel_prioridad);
CREATE INDEX idx_alerta_codigo ON alerta_fraude(codigo_alerta);
CREATE INDEX idx_caso_codigo ON caso_fraude(codigo_caso);
CREATE INDEX idx_caso_estado ON caso_fraude(estado_caso);
CREATE INDEX idx_auditoria_entidad ON auditoria(entidad, id_entidad);
