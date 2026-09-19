-- Migración V4: Ajuste de tipos bpchar a varchar para compatibilidad con JPA/Hibernate
ALTER TABLE cliente ALTER COLUMN codigo_pais_residencia TYPE VARCHAR(2);
ALTER TABLE cuenta ALTER COLUMN codigo_moneda TYPE VARCHAR(3);
ALTER TABLE beneficiario ALTER COLUMN codigo_pais TYPE VARCHAR(2);
ALTER TABLE transaccion ALTER COLUMN codigo_moneda TYPE VARCHAR(3);
ALTER TABLE transaccion ALTER COLUMN codigo_pais TYPE VARCHAR(2);
