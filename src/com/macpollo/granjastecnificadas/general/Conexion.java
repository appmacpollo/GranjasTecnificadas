/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.macpollo.granjastecnificadas.general;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Oficina
 */
public class Conexion {

    private static String servidor;
    private static String baseDatos;
    private static String usuario;

    /**
     * Metodo para trar las propiedades de la conexion a la base de datos
     */
    public static void traerPropiedades() {
        try ( InputStream input = new FileInputStream("config/basedatos.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            servidor = prop.getProperty("Servidor");
            baseDatos = prop.getProperty("BaseDatos");
            usuario = prop.getProperty("Usuario");

        } catch (IOException ex) {
            Log.escribir("Error trayendo propiedades de archivo " + ex.getMessage());
        }
    }

    /**
     * Metodo que establece la conexion a la BD con los parametrso
     *
     * @return Objeto con la conexion a la base de datos establecida
     * @throws SQLException
     */
    public static Connection getConection() {
        Connection con = null;
        try {
            String url = "jdbc:sqlserver://" + servidor + ";"
                    + "database=" + baseDatos + ";"
                    + "user=" + usuario + ";"
                    + "password=cliquida..*8;"
                    + "encrypt=false;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=5;";

            con = DriverManager.getConnection(url);
        } catch (Exception e) {
            Log.escribir("Error generando la conexion a la Base de Datos" + e.getMessage());
        }
        return con;
    }
}
