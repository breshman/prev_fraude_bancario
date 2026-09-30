-- ====================================================================
-- Migración V5: Sincronización de secuencias seriales en PostgreSQL
-- Corrige el desajuste entre IDs semilla insertados y los punteros
-- de secuencias (evita duplicate key value violates unique constraint)
-- ====================================================================

SELECT setval(pg_get_serial_sequence('canal', 'id_canal'), COALESCE((SELECT MAX(id_canal) FROM canal), 1));
SELECT setval(pg_get_serial_sequence('tipo_transaccion', 'id_tipo_transaccion'), COALESCE((SELECT MAX(id_tipo_transaccion) FROM tipo_transaccion), 1));
SELECT setval(pg_get_serial_sequence('rol', 'id_rol'), COALESCE((SELECT MAX(id_rol) FROM rol), 1));
SELECT setval(pg_get_serial_sequence('usuario_sistema', 'id_usuario'), COALESCE((SELECT MAX(id_usuario) FROM usuario_sistema), 1));
SELECT setval(pg_get_serial_sequence('cliente', 'id_cliente'), COALESCE((SELECT MAX(id_cliente) FROM cliente), 1));
SELECT setval(pg_get_serial_sequence('cuenta', 'id_cuenta'), COALESCE((SELECT MAX(id_cuenta) FROM cuenta), 1));
SELECT setval(pg_get_serial_sequence('beneficiario', 'id_beneficiario'), COALESCE((SELECT MAX(id_beneficiario) FROM beneficiario), 1));
SELECT setval(pg_get_serial_sequence('dispositivo', 'id_dispositivo'), COALESCE((SELECT MAX(id_dispositivo) FROM dispositivo), 1));
SELECT setval(pg_get_serial_sequence('transaccion', 'id_transaccion'), COALESCE((SELECT MAX(id_transaccion) FROM transaccion), 1));
SELECT setval(pg_get_serial_sequence('regla_fraude', 'id_regla'), COALESCE((SELECT MAX(id_regla) FROM regla_fraude), 1));
SELECT setval(pg_get_serial_sequence('evaluacion_fraude', 'id_evaluacion'), COALESCE((SELECT MAX(id_evaluacion) FROM evaluacion_fraude), 1));
SELECT setval(pg_get_serial_sequence('evaluacion_regla', 'id_evaluacion_regla'), COALESCE((SELECT MAX(id_evaluacion_regla) FROM evaluacion_regla), 1));
SELECT setval(pg_get_serial_sequence('alerta_fraude', 'id_alerta'), COALESCE((SELECT MAX(id_alerta) FROM alerta_fraude), 1));
SELECT setval(pg_get_serial_sequence('caso_fraude', 'id_caso'), COALESCE((SELECT MAX(id_caso) FROM caso_fraude), 1));
SELECT setval(pg_get_serial_sequence('historial_caso', 'id_historial'), COALESCE((SELECT MAX(id_historial) FROM historial_caso), 1));
SELECT setval(pg_get_serial_sequence('auditoria', 'id_auditoria'), COALESCE((SELECT MAX(id_auditoria) FROM auditoria), 1));
