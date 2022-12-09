/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.general;

import com.macpollo.granjastecnificadas.models.LogEnvioSap;
import com.macpollo.granjastecnificadas.models.LoteGalponVariable;
import com.macpollo.granjastecnificadas.models.ValidacionTolerancia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.crypto.Mac;

/**
 *
 * @author Oficina
 */
public class Proceso {

    private Connection conexion;

    public Proceso() {
        this.conexion = Conexion.getConection();
    }

    public boolean conexionExistosa() {
        return this.conexion != null;
    }

    public boolean procesarDatosGranjasTecnificadas() throws SQLException {
        //Consumo 
        List<String> arParametrosConsulta = new ArrayList<String>(Arrays.asList("FeedTotalPerDay", "SiloName1", "FeedPerAnimalPerDay", "FeedPerBirdPerDayFemale", "FeedPerBirdPerDayMale"));
        //Peso
        arParametrosConsulta.addAll(Arrays.asList("BirdScaleWeight1", "BirdScaleWeight2", "BroilerGroupWeightFemale", "BroilerGroupWeightMale"));
        //Mortalidad
        arParametrosConsulta.addAll(Arrays.asList("BroilerGroupMortalityFemale", "BroilerGroupMortalityMale", "BroilerGroupTotalStockedFemale", "BroilerGroupTotalStockedMale"));

        arParametrosConsulta.add("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date fechaDesde = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date fechaHasta = cal.getTime();

        ArrayList<String> arGranjasProcesar = new ArrayList<>();
        try ( PreparedStatement ps = conexion.prepareStatement("select granja from tblgranjastecnificadas")) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                arGranjasProcesar.add(res.getString("granja"));
            }
        }

        ArrayList<ValidacionTolerancia> arValidaciones = new ArrayList<>();
        try ( PreparedStatement ps = conexion.prepareStatement("select * from tblvalidaciones")) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                arValidaciones.add(new ValidacionTolerancia(res.getInt("edad"), res.getString("sexo"), res.getDouble("consumominimo"),
                        res.getDouble("consumomaximo"), res.getDouble("pesominimo"), res.getDouble("pesomaximo"),
                        res.getDouble("mortalidadminimo"), res.getDouble("mortalidadmaximo")));
            }
        }
//        System.out.println("Validacion ------------");
//        System.out.println(arValidaciones);

//        System.out.println("LoteGalpon ------------");
        HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja = new HashMap<>();
        HashMap<String, ArrayList<String>> hashGalponXGranja = new HashMap<>();
        if (arGranjasProcesar.size() > 0) {
            for (String granja : arGranjasProcesar) {
                ArrayList<String> arGalponesXGranja = new ArrayList<>();

                ArrayList<LoteGalponVariable> arDatos = new ArrayList<>();
                String inParamas = String.join(",", arParametrosConsulta.stream().map(x -> "?").collect(Collectors.toList()));
                String statement = "select * from tbllotegalpon where Granja like ? AND timestamp BETWEEN ? AND ? AND Variable IN(" + inParamas + ")";
                try ( PreparedStatement ps = conexion.prepareStatement(statement)) {
                    ps.setString(1, granja + "%");
                    ps.setTimestamp(2, new Timestamp(fechaDesde.getTime()));
                    ps.setTimestamp(3, new Timestamp(fechaHasta.getTime()));
//                    System.out.println("Granja =>" + granja + "%"+" Desde => "+sdf.format(fechaDesde)+" Hasta => "+sdf.format(fechaHasta));
                    for (int i = 0; i < arParametrosConsulta.size(); i++) {
                        ps.setString(i + 4, arParametrosConsulta.get(i));
                    }

                    ResultSet res = ps.executeQuery();
                    while (res.next()) {
                        LoteGalponVariable loteGalponVariable = new LoteGalponVariable();
                        loteGalponVariable.setCodGranja(granja);
                        loteGalponVariable.setGranja(res.getString("granja"));
                        loteGalponVariable.setGalpon(res.getString("galpon"));
                        loteGalponVariable.setLote(res.getString("lote"));
                        loteGalponVariable.setEdad(res.getInt("edad"));
                        loteGalponVariable.setVariable(res.getString("variable"));
                        loteGalponVariable.setValor(res.getString("value"));
                        loteGalponVariable.setTimestamp(new Date(res.getTimestamp("timestamp").getTime()));
                        arDatos.add(loteGalponVariable);
                        if (!arGalponesXGranja.contains(res.getString("galpon"))) {
                            arGalponesXGranja.add(res.getString("galpon"));
                        }
                    }
                }
                hashGalponXGranja.put(granja, arGalponesXGranja);
                hashDatosGranja.put(granja, arDatos);
            }
        } else {
            Log.escribir("No se existen registros en tblgranjastecnificadas para procesar");
        }

        ArrayList<LogEnvioSap> arLogEnvioSap = new ArrayList<>();
        System.out.println("----- HashDatosGranja ------");
        System.out.println(hashDatosGranja);
        System.out.println("----- FinHashDatosGranja ------");
        hashGalponXGranja.forEach((granja, arGalpones) -> {
            for (String galpon : arGalpones) {
                //LoteGalponGeneric
                LoteGalponVariable loteGalponVaribale = this.obtenerLoteGalponDeHash(hashDatosGranja, granja, galpon);
                String lote = loteGalponVaribale.getLote();
                Integer edad = loteGalponVaribale.getEdad();

                HashMap<String, ValidacionTolerancia> mapValidaciones = new HashMap<>();
                mapValidaciones.put("M", this.obtenerValidacion(arValidaciones, edad, "M"));
                mapValidaciones.put("H", this.obtenerValidacion(arValidaciones, edad, "H"));
                mapValidaciones.put("X", this.obtenerValidacion(arValidaciones, edad, "X"));
                //Validacion de Consumo
                this.realizarValidacionConsumo(arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones);
                //Validacion de Peso
                this.realizarValidacionPeso(arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones);
                //Validacion de Mortalidad
                this.realizarValidacionMortalidad(arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones);
            }
        });

        System.out.println("Validacion total--------------------");
        System.out.println(arLogEnvioSap.size());
        arLogEnvioSap.stream().forEach(x -> System.out.println("Granja: " + x.getGranja() + ", Galpon: " + x.getGalpon()
                + ", estado: " + (x.getEstado() ? "True" : "False") + ", Variable:" + x.getVariable() + ", Observacion:" + x.getObservacion()));

//        System.out.println(hashDatosGranja);
        return true;
    }

    public void realizarValidacionConsumo(ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
            LoteGalponVariable loteGalponVaribale, HashMap<String, ValidacionTolerancia> mapValidaciones) {

        String granja = loteGalponVaribale.getCodGranja();
        String galpon = loteGalponVaribale.getGalpon();
        Integer edad = loteGalponVaribale.getEdad();
        String[] splitLote = loteGalponVaribale.getLote().split("-");

        ValidacionTolerancia validacionMacho = mapValidaciones.get("M");
        ValidacionTolerancia validacionHembra = mapValidaciones.get("H");
        ValidacionTolerancia validacionMixto = mapValidaciones.get("X");

        String siloName = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "SiloName1");
        String feedTotalPerDay = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedTotalPerDay");
        String feedPerAnimalPerDay = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedPerAnimalPerDay");
        String feedPerBirdPerDayFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedPerBirdPerDayFemale");
        String feedPerBirdPerDayMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedPerBirdPerDayMale");

        //Valido si alguno de los "3" valores no viene genero log por ausencia de variables
        if (siloName.isEmpty() || feedTotalPerDay.isEmpty() || (feedPerAnimalPerDay.isEmpty() && feedPerBirdPerDayFemale.isEmpty() && feedPerBirdPerDayMale.isEmpty())) {
            if (siloName.isEmpty()) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "SiloName1", "Ausencia de variable", "", "", false);
            }
            if (feedTotalPerDay.isEmpty()) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedTotalPerDay", "Ausencia de variable", "", "", false);
            }
            if ((feedPerAnimalPerDay.isEmpty() && feedPerBirdPerDayFemale.isEmpty() && feedPerBirdPerDayMale.isEmpty())) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay|FeedPerBirdPerDayFemale|FeedPerBirdPerDayMale",
                        "Ausencia de variable", "", "", false);
            }
        } else {
            if (feedPerAnimalPerDay.isEmpty()) {
                System.out.println("ACA ENTRO SI ES ALIMENTO POR DIA PERO PS NO SE SABE SI MIXTO O COMO");
            } else {
                if (!feedPerBirdPerDayFemale.isEmpty()) {
                    Double feedPerBirdPerDayFemaleDouble = Double.parseDouble(feedPerBirdPerDayFemale);
                    if (feedPerBirdPerDayFemaleDouble < validacionHembra.getConsumoMinimo() || feedPerBirdPerDayFemaleDouble > validacionHembra.getConsumoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerBirdPerDayFemale",
                                "El Consumo " + feedPerBirdPerDayFemaleDouble + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                    } else {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerBirdPerDayFemale",
                                "Se registra consumo Hembra", "", "", true);
                    }
                } else {
                    Double feedPerBirdPerDayMaleDouble = Double.parseDouble(feedPerBirdPerDayMale);
                    if (feedPerBirdPerDayMaleDouble < validacionMacho.getConsumoMinimo() || feedPerBirdPerDayMaleDouble > validacionMacho.getConsumoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerBirdPerDayMale",
                                "El Consumo " + feedPerBirdPerDayMaleDouble + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                    } else {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerBirdPerDayFemale",
                                "Se registra consumo Macho", "", "", true);
                    }
                }
            }
        }

        //Comienzo a validar el SiloName1 y capacidad material
    }

    public void realizarValidacionPeso(ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
            LoteGalponVariable loteGalponVaribale, HashMap<String, ValidacionTolerancia> mapValidaciones) {
        String granja = loteGalponVaribale.getCodGranja();
        String galpon = loteGalponVaribale.getGalpon();
        Integer edad = loteGalponVaribale.getEdad();

        ValidacionTolerancia validacionMacho = mapValidaciones.get("M");
        ValidacionTolerancia validacionHembra = mapValidaciones.get("H");
        ValidacionTolerancia validacionMixto = mapValidaciones.get("X");

        String birdScaleWeight1 = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BirdScaleWeight1");
        String birdScaleWeight2 = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BirdScaleWeight2");
        String broilerGroupWeightFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupWeightFemale");
        String broilerGroupWeightMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupWeightMale");

        if (birdScaleWeight1.isEmpty() && birdScaleWeight2.isEmpty() && broilerGroupWeightMale.isEmpty() && broilerGroupWeightFemale.isEmpty()) {
            this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2|BroilerGroupWeightFemale|BroilerGroupWeightMale",
                    "Ausencia de variable", "", "", false);
        } else if (!birdScaleWeight1.isEmpty() || !birdScaleWeight2.isEmpty()) {
            Double birdScaleWeight1Double = Double.parseDouble((birdScaleWeight1.isEmpty()) ? "0" : birdScaleWeight1);
            Double birdScaleWeight2Double = Double.parseDouble((birdScaleWeight2.isEmpty()) ? "0" : birdScaleWeight2);
            int cantidadValores = (birdScaleWeight1Double > 0 && birdScaleWeight2Double > 0) ? 2 : 1;

            Double totalWeigth = (birdScaleWeight1Double + birdScaleWeight2Double) / cantidadValores;
            //PENDIENTE REVISAR SI MACHO O HEMBRA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        } else {
            Double broilerGroupWeightFemaleDouble = Double.parseDouble((broilerGroupWeightFemale.isEmpty()) ? "0" : broilerGroupWeightFemale);
            Double broilerGroupWeightMaleDouble = Double.parseDouble((broilerGroupWeightMale.isEmpty()) ? "0" : broilerGroupWeightMale);
            Double totalWeigth;
            if (broilerGroupWeightFemaleDouble > 0 && broilerGroupWeightMaleDouble > 0) {
                totalWeigth = (broilerGroupWeightFemaleDouble + broilerGroupWeightMaleDouble) / 2;
                //PENDIENTE REVISAR SI MACHO O HEMBRA O MIXTO (Asumo MIXTO)
            } else if (broilerGroupWeightFemaleDouble > 0) {
                totalWeigth = broilerGroupWeightFemaleDouble;
                if (totalWeigth < validacionHembra.getPesoMinimo() || totalWeigth > validacionHembra.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale",
                            "Se registra peso Hembra", "", "", true);
                }
            } else {
                totalWeigth = broilerGroupWeightMaleDouble;
                if (totalWeigth < validacionMacho.getPesoMinimo() || totalWeigth > validacionMacho.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightMale",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightMale",
                            "Se registra peso Macho", "", "", true);
                }
            }
        }
    }

    public void realizarValidacionMortalidad(ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
            LoteGalponVariable loteGalponVaribale, HashMap<String, ValidacionTolerancia> mapValidaciones) {

        String granja = loteGalponVaribale.getCodGranja();
        String galpon = loteGalponVaribale.getGalpon();
        Integer edad = loteGalponVaribale.getEdad();

        ValidacionTolerancia validacionMacho = mapValidaciones.get("M");
        ValidacionTolerancia validacionHembra = mapValidaciones.get("H");
        ValidacionTolerancia validacionMixto = mapValidaciones.get("X");

        String broilerGroupMortalityFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupMortalityFemale");
        String broilerGroupMortalityMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupMortalityMale");
        String broilerGroupTotalStockedFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedFemale");
        String broilerGroupTotalStockedMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedMale");

        Double broilerGroupMortalityFemaleDouble = Double.valueOf(broilerGroupMortalityFemale.isEmpty() ? "0" : broilerGroupMortalityFemale);
        Double broilerGroupMortalityMaleDouble = Double.valueOf(broilerGroupMortalityMale.isEmpty() ? "0" : broilerGroupMortalityMale);
        Double broilerGroupTotalStockedFemaleDouble = Double.valueOf(broilerGroupTotalStockedFemale.isEmpty() ? "0" : broilerGroupTotalStockedFemale);
        Double broilerGroupTotalStockedMaleDouble = Double.valueOf(broilerGroupTotalStockedMale.isEmpty() ? "0" : broilerGroupTotalStockedMale);

        if (broilerGroupMortalityFemale.isEmpty() && broilerGroupMortalityMale.isEmpty() && broilerGroupTotalStockedFemale.isEmpty() && broilerGroupTotalStockedMale.isEmpty()) {
            this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BroilerGroupMortalityMale|BroilerGroupTotalStockedFemale|BroilerGroupTotalStockedMale",
                    "Ausencia de variables", "", "", false);
        }
        if ((broilerGroupMortalityFemale.isEmpty() || broilerGroupTotalStockedFemale.isEmpty()) && (broilerGroupMortalityMale.isEmpty() || broilerGroupTotalStockedMale.isEmpty())) {
            ArrayList<String> variablesAusentes = new ArrayList<>();
            if (broilerGroupMortalityFemale.isEmpty()) {
                variablesAusentes.add("BroilerGroupMortalityFemale");
            }
            if (broilerGroupTotalStockedFemale.isEmpty()) {
                variablesAusentes.add("BroilerGroupTotalStockedFemale");
            }
            if (broilerGroupMortalityMale.isEmpty()) {
                variablesAusentes.add("BroilerGroupMortalityMale");
            }
            if (broilerGroupTotalStockedMale.isEmpty()) {
                variablesAusentes.add("BroilerGroupTotalStockedMale");
            }
            this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, String.join("|", variablesAusentes),
                    "Ausencia de variables", "", "", false);
        } else {
            Double mortalidadFemale = broilerGroupMortalityFemaleDouble * broilerGroupTotalStockedFemaleDouble;
            Double mortalidadMale = broilerGroupMortalityMaleDouble * broilerGroupTotalStockedMaleDouble;
            if (mortalidadFemale > 0 && mortalidadMale > 0) {
                Double mortalidadMixto = (mortalidadFemale + mortalidadMale) / 2;
                if (mortalidadMixto < validacionMixto.getMortalidadMinima() || mortalidadMixto > validacionMixto.getMortalidadMaxima()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BroilerGroupTotalStockedFemale|BroilerGroupMortalityMale|BroilerGroupTotalStockedMale",
                            "La Mortalidad Mixta " + mortalidadMixto + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BroilerGroupTotalStockedFemale|BroilerGroupMortalityMale|BroilerGroupTotalStockedMale",
                            "Se registra mortalidad Mixta", "", "", true);
                }
            } else if (!broilerGroupMortalityFemale.isEmpty() && !broilerGroupTotalStockedFemale.isEmpty()) {
                if (mortalidadFemale < validacionHembra.getMortalidadMinima() || mortalidadFemale > validacionHembra.getMortalidadMaxima()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BroilerGroupTotalStockedFemale",
                            "La Mortalidad Hembra " + mortalidadFemale + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BroilerGroupTotalStockedFemale",
                            "Se registra mortalidad Hembra", "", "", true);
                }
            } else if (!broilerGroupMortalityMale.isEmpty() && !broilerGroupTotalStockedMale.isEmpty()) {
                if (mortalidadMale < validacionMacho.getMortalidadMinima() || mortalidadMale > validacionMacho.getMortalidadMaxima()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityMale|BroilerGroupTotalStockedMale",
                            "La Mortalidad Macho " + mortalidadMale + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityMale|BroilerGroupTotalStockedMale",
                            "Se registra mortalidad Macho", "", "", true);
                }
            }
        }
    }

    public String obtenerVariableDeHash(HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja, String granja, String galpon, String variable) {
        LoteGalponVariable loteGalponVariable = hashDatosGranja.get(granja).stream()
                .filter(x -> {
                    return (x.getGalpon().equals(galpon) && x.getVariable().equals(variable));
                }).findFirst().orElse(null);
        if (Objects.isNull(loteGalponVariable)) {
            return "";
        }
        return loteGalponVariable.getValor();
    }

    public LoteGalponVariable obtenerLoteGalponDeHash(HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja, String granja, String galpon) {
        LoteGalponVariable loteGalponVariable = hashDatosGranja.get(granja).stream()
                .filter(x -> {
                    return (x.getCodGranja().equals(granja) && x.getGalpon().equals(galpon));
                }).findFirst().orElse(null);
        return loteGalponVariable;
    }

    public ValidacionTolerancia obtenerValidacion(ArrayList<ValidacionTolerancia> arValidaciones, Integer edad, String sexo) {
        return arValidaciones.stream().filter(x -> {
            return (x.getEdad() == edad && x.getSexo().equals(sexo));
        }).findFirst().orElse(null);
    }

    public void registrarLogEnvioSap(ArrayList<LogEnvioSap> arLogEnvioSap, LoteGalponVariable loteGalponVariable, String variable, String msg,
            String docSapTecnico, String docSapInventario, boolean estado) {
        arLogEnvioSap.add(new LogEnvioSap(loteGalponVariable.getCodGranja(), loteGalponVariable.getGalpon(), loteGalponVariable.getLote(),
                loteGalponVariable.getEdad(), variable,
                new Date(), msg, docSapTecnico, docSapInventario, estado));
    }

}
