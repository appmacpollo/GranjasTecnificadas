/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.macpollo.granjastecnificadas.principal;

import com.macpollo.granjastecnificadas.models.RetornoDatosTecnicos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Oficina
 */
public class PruebasClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String x = "SiloName1";
        String[] split = x.split("\\|");
        System.out.println(Arrays.toString(split));
//        ArrayList<RetornoDatosTecnicos> arRetornoLog = new ArrayList<>();
//        RetornoDatosTecnicos x = new RetornoDatosTecnicos();
//        RetornoDatosTecnicos y = new RetornoDatosTecnicos();
//        x.setMessage("Mensaje 1");
//        y.setMessage("Mensaje 2, con otro contenido");
//        arRetornoLog.add(x);
//        arRetornoLog.add(y);
//
//        String con = arRetornoLog.stream().map(el -> el.getMessage()).collect(Collectors.joining("|"));
//        System.out.println(con);
//        System.out.println(x.contains("Doc:"));
//        String y = "Doc.Tecnico: 4957578768 Mov.261 .4957578799";
//        System.out.println(y.contains("Doc.Tecnico:"));
//        Pattern pattern = Pattern.compile("[0-9]{10}");
//        Matcher matcher = pattern.matcher(x);
//        if (matcher.find()) {
//            System.out.println(matcher.group());
//        }
//        System.out.println(x.);
    }

}
