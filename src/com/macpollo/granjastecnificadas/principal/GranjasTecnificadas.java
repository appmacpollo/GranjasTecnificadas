/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.macpollo.granjastecnificadas.principal;

import com.macpollo.granjastecnificadas.componentes.Automatico;
import com.macpollo.granjastecnificadas.general.Conexion;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Steven Munoz
 */
public class GranjasTecnificadas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        Comentar esto para que imprima en consola Y NO NOLVIDAR DESCOMENTARLO DESPUES
        File file = new File("logGranjasTecnificadas.log");
        PrintStream stream = new PrintStream(file);
        File fileError = new File("errorGranjasTecnificadas.log");
        PrintStream streamError = new PrintStream(fileError);
        System.out.println("Impesion en el archivo " + file.getAbsolutePath());
        System.setOut(stream);
        System.setErr(streamError);
        //Hasta aca

        String appId = "GranjasTecnificadas";
        boolean ejecutando;

        try {
            JUnique.acquireLock(appId);
            ejecutando = false;
        } catch (AlreadyLockedException ex) {
            ejecutando = true;
        }

        if (!ejecutando) {
            Conexion.traerPropiedades();
            Automatico.inicial();
        } else {
            System.out.println("Se detecta que ya hay un programa corriendo en el equipo");
        }
    }

}
