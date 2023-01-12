/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.macpollo.granjastecnificadas.principal;

import com.macpollo.granjastecnificadas.componentes.Automatico;
import com.macpollo.granjastecnificadas.general.Conexion;
import com.macpollo.granjastecnificadas.general.Log;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author Steven Munoz
 */
public class GranjasTecnificadas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String appId = "GranjasTecnificadas";
        boolean ejecutando;

        try {
            JUnique.acquireLock(appId);
            ejecutando = false;
        } catch (AlreadyLockedException ex) {
            ejecutando = true;
        }

        // se comenta mientras se implementa lo de consulta ot
        if (!ejecutando) {
            Conexion.traerPropiedades();
//            if (Proceso.validarVersion()) {
//                if (args.length > 0 && args[0].trim().equals("A")) {
            Automatico.inicial();
//                } else {
//                    EnviarOTTorreControl eottc = new EnviarOTTorreControl();
//                    eottc.setVisible(true);
//                }
//            } else {
//                String msj = "La versión del programa " + Parametros.version + " no es la última disponible por favor actualicela";
//                Log.escribir(msj);
//                if (args.length == 0 || (args.length > 0 && !args[0].trim().equals("A"))) {
//                    JOptionPane.showMessageDialog(null, msj, "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
//                }
//            }
        } else {
            Log.escribir("Se detecta que ya hay un programa corriendo en el equipo");
        }
    }

}
