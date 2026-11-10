-- ====================================================================
-- Script: 004_datos_semilla_ventas_cajas.sql
-- Propósito: Generar abundantes datos semilla de ventas de boletos,
-- funciones programadas y sesiones de cajas desde 1 mes atrás hasta
-- 1 mes adelante, operadas por nuevos cajeros en las cajas físicas.
--
-- Ejecutar completo en PostgreSQL. Es repetible e idempotente.
-- ====================================================================

BEGIN;

-- 1. Asegurar estructura de cajas y columnas necesarias
CREATE TABLE IF NOT EXISTS public.Caja (
    id_caja SERIAL PRIMARY KEY,
    numero_caja integer NOT NULL UNIQUE,
    nombre text NOT NULL,
    estado text NOT NULL DEFAULT 'DISPONIBLE',
    id_usuario_actual integer,
    fecha_apertura timestamp without time zone
);

CREATE TABLE IF NOT EXISTS public.SesionCaja (
    id_sesion_caja SERIAL PRIMARY KEY,
    id_caja integer NOT NULL,
    id_usuario integer NOT NULL,
    fecha_inicio timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin timestamp without time zone,
    total_vendido numeric(10,2) DEFAULT 0.00,
    tickets_vendidos integer DEFAULT 0
);

ALTER TABLE public.Ticket ADD COLUMN IF NOT EXISTS id_caja integer;

-- 2. Asegurar el catálogo de 6 cajas físicas
INSERT INTO public.Caja (numero_caja, nombre, estado)
VALUES
    (1, 'Caja 01 - Taquilla Principal', 'DISPONIBLE'),
    (2, 'Caja 02 - Taquilla Central', 'DISPONIBLE'),
    (3, 'Caja 03 - Taquilla Lateral', 'DISPONIBLE'),
    (4, 'Caja 04 - Taquilla Preferencial', 'DISPONIBLE'),
    (5, 'Caja 05 - Taquilla Rápida', 'DISPONIBLE'),
    (6, 'Caja 06 - Taquilla Express', 'DISPONIBLE')
ON CONFLICT (numero_caja) DO NOTHING;

-- 3. Crear nuevos cajeros si no existen
DO $$
DECLARE
    nuevo RECORD;
BEGIN
    FOR nuevo IN SELECT * FROM (VALUES
        ('cajero_laura', 'Laura Martínez', 'laura.martinez@cine.com', '05123456-1', '7890-1231', 'F'::text),
        ('cajero_kevin', 'Kevin Rivas', 'kevin.rivas@cine.com', '05123456-2', '7890-1232', 'M'::text),
        ('cajero_sofia', 'Sofía Mendoza', 'sofia.mendoza@cine.com', '05123456-3', '7890-1233', 'F'::text),
        ('cajero_diego', 'Diego Castillo', 'diego.castillo@cine.com', '05123456-4', '7890-1234', 'M'::text),
        ('cajero_valeria', 'Valeria Benítez', 'valeria.benitez@cine.com', '05123456-5', '7890-1235', 'F'::text),
        ('cajero_alejandro', 'Alejandro Ramos', 'alejandro.ramos@cine.com', '05123456-6', '7890-1236', 'M'::text)
    ) AS u(username, nombre, email, dui, tel, gen) LOOP
        IF NOT EXISTS (SELECT 1 FROM public.Usuario WHERE username = nuevo.username OR email = nuevo.email) THEN
            INSERT INTO public.Usuario (rol, nombre, username, password_hash, dui, email, telefono, fecha_nacimiento, genero, direccion, fecha_contratacion, estado)
            VALUES (
                'Cajero',
                nuevo.nombre,
                nuevo.username,
                'pbkdf2$100000$cajero123',
                nuevo.dui,
                nuevo.email,
                nuevo.tel,
                '1998-05-15',
                nuevo.gen,
                'San Salvador, El Salvador',
                CURRENT_DATE - INTERVAL '6 months',
                'Activo'
            );
        END IF;
    END LOOP;
END $$;

-- 4. Asegurar que existan películas activas en cartelera
DO $$
DECLARE
    pel RECORD;
BEGIN
    FOR pel IN SELECT * FROM (VALUES
        ('Intensamente 2', 'Riley entra a la adolescencia y nuevas emociones llegan al cuartel general.', 96, 'Animación / Aventura', 'Kelsey Mann', CURRENT_DATE - 60, 'Normal'),
        ('Dune: Parte Dos', 'Paul Atreides se une a los Fremen para vengar la caída de su familia.', 166, 'Ciencia Ficción / Drama', 'Denis Villeneuve', CURRENT_DATE - 90, 'Normal'),
        ('Deadpool & Wolverine', 'Wade Wilson recluta a una variante de Logan para salvar su universo.', 128, 'Acción / Comedia', 'Shawn Levy', CURRENT_DATE - 45, 'Normal'),
        ('Gladiador II', 'Años después de Máximo, Lucio lucha por su honor en el Coliseo romano.', 148, 'Acción / Épica', 'Ridley Scott', CURRENT_DATE - 15, 'Estreno'),
        ('Moana 2', 'Moana y Maui se embarcan en un nuevo viaje por los mares lejanos.', 100, 'Animación / Musical', 'David Derrick Jr.', CURRENT_DATE - 5, 'Estreno'),
        ('Beetlejuice Beetlejuice', 'Tres generaciones de la familia Deetz regresan tras una tragedia inesperada.', 105, 'Comedia / Fantasía', 'Tim Burton', CURRENT_DATE - 30, 'Normal')
    ) AS p(nombre, sinopsis, duracion, genero, director, estreno, tipo) LOOP
        IF NOT EXISTS (SELECT 1 FROM public.Pelicula WHERE nombre = pel.nombre) THEN
            INSERT INTO public.Pelicula (nombre, sinopsis, duracion, genero, director, fecha_estreno, tipo_estreno, estado)
            VALUES (pel.nombre, pel.sinopsis, pel.duracion, pel.genero, pel.director, pel.estreno, pel.tipo, 'CARTELERA');
        END IF;
    END LOOP;
END $$;

-- 5. Generar funciones diarias desde 30 días atrás hasta 30 días adelante
DO $$
DECLARE
    r_dia date;
    r_pelicula record;
    r_sala record;
    horarios time[] := ARRAY['14:00:00'::time, '16:30:00'::time, '19:00:00'::time, '21:30:00'::time];
    h_idx integer;
    id_pel_arr integer[];
    id_sala_arr integer[];
    total_pels integer;
    total_salas integer;
    p_id integer;
    s_id integer;
    duracion_min integer;
    h_inicio time;
    h_fin time;
BEGIN
    SELECT array_agg(id_pelicula) INTO id_pel_arr FROM public.Pelicula WHERE estado = 'CARTELERA';
    SELECT array_agg(id_sala) INTO id_sala_arr FROM public.Sala WHERE estado = 'ACTIVA';

    total_pels := COALESCE(array_length(id_pel_arr, 1), 0);
    total_salas := COALESCE(array_length(id_sala_arr, 1), 0);

    IF total_pels > 0 AND total_salas > 0 THEN
        FOR r_dia IN SELECT generate_series(CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '30 days', '1 day'::interval)::date LOOP
            FOR h_idx IN 1..4 LOOP
                p_id := id_pel_arr[1 + ((extract(day from r_dia)::int + h_idx) % total_pels)];
                s_id := id_sala_arr[1 + ((extract(day from r_dia)::int + h_idx * 2) % total_salas)];
                h_inicio := horarios[h_idx];
                h_fin := (h_inicio + INTERVAL '2 hours')::time;

                IF NOT EXISTS (SELECT 1 FROM public.Funcion WHERE id_sala = s_id AND fecha_proyeccion = r_dia AND hora_inicio = h_inicio) THEN
                    INSERT INTO public.Funcion (id_pelicula, id_sala, fecha_proyeccion, hora_inicio, hora_fin, estado)
                    VALUES (p_id, s_id, r_dia, h_inicio, h_fin, 'Programada');
                END IF;
            END LOOP;
        END LOOP;
    END IF;
END $$;

-- 6. Generar abundantes ventas de boletos (Tickets) por función, caja y cajero
DO $$
DECLARE
    r_func RECORD;
    r_asiento RECORD;
    cajeros_ids integer[];
    cajas_ids integer[];
    cajeros_len integer;
    cajas_len integer;
    boletos_a_vender integer;
    boletos_vendidos integer;
    id_cajero_elegido integer;
    id_caja_elegida integer;
    monto_boleto numeric(10,2);
    fecha_compra timestamp;
BEGIN
    -- Lista de IDs de cajeros disponibles
    SELECT array_agg(id_usuario) INTO cajeros_ids
    FROM public.Usuario
    WHERE rol = 'Cajero' AND estado = 'Activo';

    -- Si no hay cajeros, usar el usuario 1 o cualquier usuario
    IF cajeros_ids IS NULL OR array_length(cajeros_ids, 1) = 0 THEN
        SELECT array_agg(id_usuario) INTO cajeros_ids FROM public.Usuario;
    END IF;

    -- Lista de IDs de cajas ordenadas por número
    SELECT array_agg(c.id_caja) INTO cajas_ids
    FROM (SELECT id_caja FROM public.Caja ORDER BY numero_caja) c;

    IF cajas_ids IS NULL OR array_length(cajas_ids, 1) = 0 THEN
        cajas_ids := ARRAY[1];
    END IF;
    IF cajeros_ids IS NULL OR array_length(cajeros_ids, 1) = 0 THEN
        cajeros_ids := ARRAY[1];
    END IF;

    cajeros_len := COALESCE(array_length(cajeros_ids, 1), 1);
    cajas_len := COALESCE(array_length(cajas_ids, 1), 1);

    -- Recorrer cada función entre 30 días atrás y 30 días adelante
    FOR r_func IN
        SELECT f.id_funcion, f.id_sala, f.fecha_proyeccion, f.hora_inicio
        FROM public.Funcion f
        WHERE f.fecha_proyeccion >= CURRENT_DATE - INTERVAL '30 days'
          AND f.fecha_proyeccion <= CURRENT_DATE + INTERVAL '30 days'
        ORDER BY f.fecha_proyeccion, f.hora_inicio
    LOOP
        -- Cantidad de boletos a vender según si es pasada o futura (preventa)
        IF r_func.fecha_proyeccion <= CURRENT_DATE THEN
            -- Funciones pasadas: entre 18 y 36 boletos vendidos
            boletos_a_vender := 18 + (r_func.id_funcion % 19);
        ELSE
            -- Funciones futuras (preventa): entre 6 y 14 boletos vendidos
            boletos_a_vender := 6 + (r_func.id_funcion % 9);
        END IF;

        boletos_vendidos := 0;

        -- Asignar caja y cajero consistentes por función o bloques
        id_cajero_elegido := cajeros_ids[1 + (r_func.id_funcion % cajeros_len)];
        id_caja_elegida := cajas_ids[1 + (r_func.id_funcion % cajas_len)];

        -- Buscar asientos disponibles de la sala para esta función
        FOR r_asiento IN
            SELECT a.id_asiento, a.tipo_de_asiento
            FROM public.Asiento a
            WHERE a.id_sala = r_func.id_sala
              AND NOT EXISTS (
                  SELECT 1 FROM public.Ticket t
                  WHERE t.id_funcion = r_func.id_funcion AND t.id_asiento = a.id_asiento
              )
            ORDER BY a.fila, a.numero
            LIMIT boletos_a_vender
        LOOP
            -- Precio según tipo de asiento
            IF r_asiento.tipo_de_asiento = 'Especial' THEN
                monto_boleto := 6.00;
            ELSE
                monto_boleto := 4.50;
            END IF;

            -- Fecha y hora de compra
            IF r_func.fecha_proyeccion <= CURRENT_DATE THEN
                -- Venta realizada el día de la función antes de la hora de inicio
                fecha_compra := (r_func.fecha_proyeccion + TIME '09:30:00')
                    + (((boletos_vendidos * 17) % 600) * INTERVAL '1 minute');
            ELSE
                -- Preventa realizada en los últimos 5 días
                fecha_compra := (CURRENT_DATE - INTERVAL '3 days')
                    + (((r_func.id_funcion * 7 + boletos_vendidos * 23) % 4320) * INTERVAL '1 minute');
            END IF;

            -- Alternar cajero y caja cada varios boletos para simular múltiples operadores
            IF boletos_vendidos % 8 = 0 THEN
                id_cajero_elegido := cajeros_ids[1 + ((r_func.id_funcion + boletos_vendidos) % cajeros_len)];
                id_caja_elegida := cajas_ids[1 + ((r_func.id_funcion + boletos_vendidos) % cajas_len)];
            END IF;

            INSERT INTO public.Ticket (id_funcion, id_asiento, id_usuario, monto, fecha_hora_compra, id_caja)
            VALUES (r_func.id_funcion, r_asiento.id_asiento, id_cajero_elegido, monto_boleto, fecha_compra, id_caja_elegida)
            ON CONFLICT DO NOTHING;

            boletos_vendidos := boletos_vendidos + 1;
        END LOOP;
    END LOOP;
END $$;

-- 7. Generar historial de sesiones de caja (SesionCaja) con totales cuadriculados
DO $$
DECLARE
    r_ses RECORD;
BEGIN
    FOR r_ses IN
        SELECT
            t.id_caja,
            t.id_usuario,
            DATE(t.fecha_hora_compra) AS fecha_dia,
            MIN(t.fecha_hora_compra) AS inicio,
            MAX(t.fecha_hora_compra) AS fin,
            COUNT(t.id_ticket) AS total_tks,
            COALESCE(SUM(t.monto), 0) AS total_monto
        FROM public.Ticket t
        WHERE t.id_caja IS NOT NULL AND t.id_usuario IS NOT NULL
        GROUP BY t.id_caja, t.id_usuario, DATE(t.fecha_hora_compra)
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM public.SesionCaja
            WHERE id_caja = r_ses.id_caja
              AND id_usuario = r_ses.id_usuario
              AND DATE(fecha_inicio) = r_ses.fecha_dia
        ) THEN
            INSERT INTO public.SesionCaja (id_caja, id_usuario, fecha_inicio, fecha_fin, total_vendido, tickets_vendidos)
            VALUES (
                r_ses.id_caja,
                r_ses.id_usuario,
                r_ses.inicio,
                r_ses.fin + INTERVAL '10 minutes',
                r_ses.total_monto,
                r_ses.total_tks
            );
        END IF;
    END LOOP;
END $$;

COMMIT;

-- ====================================================================
-- Resumen final de datos insertados
-- ====================================================================
SELECT 'Cajas registradas' AS concepto, COUNT(*)::text AS total FROM public.Caja
UNION ALL
SELECT 'Cajeros activos', COUNT(*)::text FROM public.Usuario WHERE rol = 'Cajero' AND estado = 'Activo'
UNION ALL
SELECT 'Películas en cartelera', COUNT(*)::text FROM public.Pelicula WHERE estado = 'CARTELERA'
UNION ALL
SELECT 'Funciones programadas (-30d a +30d)', COUNT(*)::text FROM public.Funcion WHERE fecha_proyeccion >= CURRENT_DATE - 30 AND fecha_proyeccion <= CURRENT_DATE + 30
UNION ALL
SELECT 'Total tickets vendidos', COUNT(*)::text FROM public.Ticket
UNION ALL
SELECT 'Recaudación total de tickets', '$ ' || TO_CHAR(COALESCE(SUM(monto), 0), 'FM999,999.00') FROM public.Ticket
UNION ALL
SELECT 'Sesiones de caja registradas', COUNT(*)::text FROM public.SesionCaja;
