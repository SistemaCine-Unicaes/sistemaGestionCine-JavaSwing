package com.mycompany.sistemagestioncine;

import com.mycompany.sistemagestioncine.config.Conexion;
import com.mycompany.sistemagestioncine.dao.FuncionDAO;
import com.mycompany.sistemagestioncine.models.Funcion;
import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public class SistemaGestionCine {

    public static void main(String[] args) {
        // 1. Iniciar la conexión
        Connection conexion = Conexion.getConexion();
        
        if (conexion != null) {
            FuncionDAO funcionDAO = new FuncionDAO(conexion);

            System.out.println("\n--- VERIFICANDO CONEXIÓN Y DATOS ---");
            // Ejecutar un SELECT buscando las funciones del día para comprobar que todo sigue en orden
            Date fechaBusqueda = Date.valueOf("2026-09-07");
            List<Funcion> funcionesHoy = funcionDAO.listarFuncionesDelDia(fechaBusqueda);
            
            if (funcionesHoy.isEmpty()) {
                System.out.println("No se encontraron funciones para la fecha " + fechaBusqueda);
            } else {
                System.out.println("Se encontraron " + funcionesHoy.size() + " función(es) para el " + fechaBusqueda + ":");
                for (Funcion f : funcionesHoy) {
                    System.out.println(" -> Función ID: " + f.getIdFuncion() + 
                                       " | Película ID: " + f.getIdPelicula() + 
                                       " | Sala ID: " + f.getIdSala() + 
                                       " | Horario: " + f.getHoraInicio() + " a " + f.getHoraFin() + 
                                       " | Estado: " + f.getEstado());
                }
            }
        }
    }
}