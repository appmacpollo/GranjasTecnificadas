/* 
 * Copyright (c) 2018, Avidesa MacPollo S.A.
 * Todos los Derechos Reservados.
 * 
 * Este es un software propietario y su contenido es confidencial de Avidesa MacPollo S.A y sus afiliados.
 * 
 * Toda la informacion contenida en el presente es y sigue siendo propiedad de Avidesa MacPollo S.A y sus
 * afiliados. Los conceptos intelectuales y tecnicos contenidos en este documento son propiedad de
 * Avidesa MacPollo S.A y sus afiliados y estan protegidos por secretos comerciales o leyes de derechos 
 * de autor. La difusion de esta informacion o reproduccion de este material esta estrictamente prohibida 
 * a menos que se obtenga el permiso previo escrito de Avidesa MacPollo S.A o sus afiliados.
 */
package com.macpollo.granjastecnificadas.general;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Clase que me escribe un log de la aplicación 
 * 
 * @author evega
 */
public class Log {
    
    /**
     * metodo para escribir en el log los mensajes
     * 
     * @param mensaje lo que se quiere escribir
     */
    public static void escribir(String mensaje) {
        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {
            String localDir = System.getProperty("user.dir");
            String ruta = localDir + "/log/" + getNombreArchivo() + ".log";
            fh = new FileHandler(ruta, true);
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info(mensaje);
            fh.close();

        } catch (SecurityException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * obtiene el nombre del archivo de log en formato yyyy-MM-dd
     * 
     * @return 
     */
    private static String getNombreArchivo() {
        Date hoy = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(hoy);
    }
    
}
