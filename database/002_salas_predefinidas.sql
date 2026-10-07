-- Ejecutar completo. Es repetible: no reemplaza salas, asientos ni ventas existentes.
ALTER TYPE estado_sala ADD VALUE IF NOT EXISTS 'MANTENIMIENTO';
BEGIN;
SELECT pg_advisory_xact_lock(20261003, 2);
ALTER TABLE Sala ADD COLUMN IF NOT EXISTS codigo_plano text;
ALTER TABLE Sala ADD COLUMN IF NOT EXISTS nombre text;
ALTER TABLE Sala ADD COLUMN IF NOT EXISTS motivo_inactividad text;
CREATE UNIQUE INDEX IF NOT EXISTS sala_codigo_plano_unico ON Sala(codigo_plano);
ALTER TABLE Asiento ADD COLUMN IF NOT EXISTS columna_plano integer;
ALTER TABLE Asiento ADD COLUMN IF NOT EXISTS fila_plano integer;
ALTER TABLE Asiento ADD COLUMN IF NOT EXISTS motivo_inactividad text;

-- Distribuciones propias inspiradas en los planos citados en database/SALAS.md.
-- Los números de asiento no cuentan los pasillos. Las coordenadas sí los reservan.
DO $$
DECLARE
    plano record;
    sala_id integer;
    capacidad integer;
    ancho integer;
    fila integer;
    numero integer;
    columna integer;
BEGIN
    FOR plano IN SELECT * FROM (VALUES
        ('CINE-01', 'Sala Boutique', ARRAY[6,6,8,8,10,10], 2, 15, 1),
        ('CINE-02', 'Sala Clásica', ARRAY[8,8,8,8,10,10,10,10], 2, 15, 1),
        ('CINE-03', 'Sala Panorámica', ARRAY[10,10,12,12,12,12,14,14], 4, 20, 2),
        ('CINE-04', 'Sala Central', ARRAY[12,12,12,12,12,12,12,12,12], 4, 20, 1),
        ('CINE-05', 'Sala Gran Formato', ARRAY[12,12,12,12,12,12,12,12,12,12,12,12], 4, 25, 2),
        ('CINE-06', 'Sala Estrenos', ARRAY[15,15,15,15,15,15,15,15,15,15,15,15], 4, 25, 2)
    ) AS p(codigo,nombre,filas,especiales,limpieza,pasillos) LOOP
        IF EXISTS (SELECT 1 FROM Sala WHERE codigo_plano=plano.codigo) THEN CONTINUE; END IF;
        SELECT sum(n),max(n) INTO capacidad,ancho FROM unnest(plano.filas) AS n;
        INSERT INTO Sala(capacidad_total,asientos_especiales,tiempo_de_limpieza,asientos_por_fila,estado,codigo_plano,nombre)
        VALUES(capacidad,plano.especiales,plano.limpieza,ancho,'ACTIVA',plano.codigo,plano.nombre)
        RETURNING id_sala INTO sala_id;
        FOR fila IN 1..array_length(plano.filas,1) LOOP
            FOR numero IN 1..plano.filas[fila] LOOP
                columna := (ancho-plano.filas[fila])/2 + numero-1;
                IF plano.pasillos=1 THEN
                    columna := columna + CASE WHEN columna >= ancho/2 THEN 1 ELSE 0 END;
                ELSE
                    columna := columna + CASE WHEN columna >= ancho*2/3 THEN 2 WHEN columna >= ancho/3 THEN 1 ELSE 0 END;
                END IF;
                INSERT INTO Asiento(id_sala,tipo_de_asiento,fila,numero,estado,columna_plano,fila_plano)
                VALUES(sala_id,CASE WHEN fila=1 AND numero<=plano.especiales THEN 'Especial' ELSE 'Normal' END,
                    chr(64+fila),numero,'Disponible',columna,
                    fila-1 + CASE WHEN array_length(plano.filas,1)>=10 AND fila>6 THEN 1 ELSE 0 END);
            END LOOP;
        END LOOP;
    END LOOP;
END $$;
COMMIT;
