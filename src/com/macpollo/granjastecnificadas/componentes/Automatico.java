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
package com.macpollo.granjastecnificadas.componentes;

import com.macpollo.granjastecnificadas.general.Log;
import com.macpollo.granjastecnificadas.general.Proceso;

/**
 * Clase que se llama cuando el programa se ejecuta de forma automatica y envia
 * todos los transportes pendientes del día
 *
 * @author evega
 */
public class Automatico {

    public static void inicial() {
        try {
            Proceso proceso = new Proceso();
            if (proceso.conexionExistosa()) {
                proceso.procesarDatosGranjasTecnificadas();
            } else {
                System.err.println("No se logro generar conexion");
                Log.escribir("No se logro generar la conexion a la Base de Datos");
            }
//            System.out.println("Aca llegamos");
//            Proceso.consultarTransportesSAP(false, null, null, null, null, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Log.escribir("En el que hace el proceso principal" + ex.getMessage());
        }
    }
}
