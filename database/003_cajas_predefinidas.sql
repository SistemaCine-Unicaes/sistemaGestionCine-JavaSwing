-- ====================================================================
-- Script: 003_cajas_predefinidas.sql
-- Propósito: Estructura de cajas preestablecidas, asignación de caja
-- a ventas de boletos e historial de sesiones de cajeros.
-- Ejecutar completo. Es repetible sin borrar datos existentes.
-- ====================================================================

BEGIN;

-- 1. Tabla de Cajas del cine
CREATE TABLE IF NOT EXISTS public.Caja (
    id_caja SERIAL PRIMARY KEY,
    numero_caja integer NOT NULL UNIQUE CONSTRAINT caja_numero_positivo CHECK (numero_caja > 0),
    nombre text NOT NULL,
    estado text NOT NULL DEFAULT 'DISPONIBLE' CONSTRAINT caja_estado_valido CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'MANTENIMIENTO', 'INACTIVA')),
    id_usuario_actual integer REFERENCES public.Usuario(id_usuario),
    fecha_apertura timestamp without time zone
);

-- 2. Vincular cada Ticket con la Caja donde fue emitido
ALTER TABLE public.Ticket ADD COLUMN IF NOT EXISTS id_caja integer REFERENCES public.Caja(id_caja);

-- 3. Tabla para auditoría e historial de sesiones de caja
CREATE TABLE IF NOT EXISTS public.SesionCaja (
    id_sesion_caja SERIAL PRIMARY KEY,
    id_caja integer NOT NULL REFERENCES public.Caja(id_caja),
    id_usuario integer NOT NULL REFERENCES public.Usuario(id_usuario),
    fecha_inicio timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin timestamp without time zone,
    total_vendido numeric(10,2) DEFAULT 0.00,
    tickets_vendidos integer DEFAULT 0
);

-- 4. Inserción de catálogo de cajas preestablecidas del cine
INSERT INTO public.Caja (numero_caja, nombre, estado)
VALUES
    (1, 'Caja 01 - Taquilla Principal', 'DISPONIBLE'),
    (2, 'Caja 02 - Taquilla Central', 'DISPONIBLE'),
    (3, 'Caja 03 - Taquilla Lateral', 'DISPONIBLE'),
    (4, 'Caja 04 - Taquilla Preferencial', 'DISPONIBLE'),
    (5, 'Caja 05 - Taquilla Rápida', 'DISPONIBLE'),
    (6, 'Caja 06 - Taquilla Express', 'DISPONIBLE')
ON CONFLICT (numero_caja) DO NOTHING;

-- 5. Para tickets históricos sin caja asignada, asociarlos a la Caja 1
UPDATE public.Ticket
SET id_caja = 1
WHERE id_caja IS NULL;

COMMIT;

-- Verificación
SELECT id_caja, numero_caja, nombre, estado FROM public.Caja ORDER BY numero_caja;
