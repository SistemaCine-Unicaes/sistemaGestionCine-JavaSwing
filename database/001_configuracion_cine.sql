-- Precio general compartido por todas las cajas. El administrador fija el primer precio.
CREATE TABLE IF NOT EXISTS public.configuracion_cine (
    id integer PRIMARY KEY CHECK (id = 1),
    precio_boleto numeric(8, 2) NOT NULL CHECK (precio_boleto > 0)
);
ALTER TABLE public.configuracion_cine ENABLE ROW LEVEL SECURITY;
-- La aplicación de escritorio usa JDBC. No se concede acceso anónimo por la API REST.
