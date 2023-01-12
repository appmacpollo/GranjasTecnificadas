/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.general;

import com.macpollo.granjastecnificadas.models.DatoTecnicoSap;
import com.macpollo.granjastecnificadas.models.LogEnvioSap;
import com.macpollo.granjastecnificadas.models.LoteGalponVariable;
import com.macpollo.granjastecnificadas.models.RetornoDatosTecnicos;
import com.macpollo.granjastecnificadas.models.TablaAlimento;
import com.macpollo.granjastecnificadas.models.TblCorreosNotificacion;
import com.macpollo.granjastecnificadas.models.ValidacionTolerancia;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        List<String> arParametrosConsulta = new ArrayList<String>(Arrays.asList("FeedTotalPerDay", "SiloName1", "FeedPerAnimalPerDay"));
        //Peso
        arParametrosConsulta.addAll(Arrays.asList("BirdScaleWeight1", "BirdScaleWeight2", "BroilerGroupWeightFemale", "BroilerGroupWeightMale"));
        //Mortalidad
        arParametrosConsulta.addAll(Arrays.asList("BroilerGroupMortalityFemale", "BroilerGroupMortalityMale", "BroilerGroupTotalStockedFemale",
                "BroilerGroupTotalStockedMale", "BirdsDeadCulledPerDayFemale", "BirdsDeadCulledPerDayMale"));

        arParametrosConsulta.add("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        //Genero rango de fechas para filtrar
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //Genero fecha para el dia anterior desde la hora 00:00:00
        Date fechaDesde = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        //Genero fecha para el dia anterior desde la hora 23:59:59
        Date fechaHasta = cal.getTime();

        //Obtengo las granjas que se van a procesar
        ArrayList<String> arGranjasProcesar = new ArrayList<>();
        try ( PreparedStatement ps = conexion.prepareStatement("select granja from tblgranjastecnificadas")) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                arGranjasProcesar.add(res.getString("granja"));
            }
        }
        //Obtengo un array con las validaciones por sexo y edad de las aves
        ArrayList<ValidacionTolerancia> arValidaciones = new ArrayList<>();
        try ( PreparedStatement ps = conexion.prepareStatement("select * from tblvalidaciones")) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                arValidaciones.add(new ValidacionTolerancia(res.getInt("edad"), res.getString("sexo"), res.getDouble("consumominimo"),
                        res.getDouble("consumomaximo"), res.getDouble("pesominimo"), res.getDouble("pesomaximo"),
                        res.getDouble("mortalidadminimo"), res.getDouble("mortalidadmaximo")));
            }
        }

        HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja = new HashMap<>();
        HashMap<String, ArrayList<String>> hashGalponXGranja = new HashMap<>();
        if (arGranjasProcesar.size() > 0) {
            for (String granja : arGranjasProcesar) {
                ArrayList<String> arGalponesXGranja = new ArrayList<>();

                ArrayList<LoteGalponVariable> arDatos = new ArrayList<>();
                String inParamas = String.join(",", arParametrosConsulta.stream().map(x -> "?").collect(Collectors.toList()));
                String statement = "select * from tbllotegalpon where Granja like ? AND Edad > 0 AND timestamp BETWEEN ? AND ? AND Variable IN(" + inParamas + ")";
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

        cal.setTime(new Date());
        //Resto 1 dia, para hacer registro granjas dia anterior
        cal.add(Calendar.DATE, -1);

        ArrayList<LogEnvioSap> arLogEnvioSap = new ArrayList<>();
        hashGalponXGranja.forEach((granja, arGalpones) -> {
            System.out.println("Granja => " + granja);
            ArrayList<DatoTecnicoSap> arDatosTecnicosSap = new ArrayList<>();
            if (arGalpones.isEmpty()) {
                arLogEnvioSap.add(new LogEnvioSap(granja, granja,
                        "0", "", 0, "Sin Registros",
                        new Date(), "No se encontraron registros de datos para la granja " + granja, "", "", false));
            }
            for (String galpon : arGalpones) {
                //DatoTecnico a registrar en SAP
                ArrayList<DatoTecnicoSap> arDatosTecnicosExtras = new ArrayList<>();
                DatoTecnicoSap datoTecnicoSap = new DatoTecnicoSap();
                //LoteGalponGeneric
                LoteGalponVariable loteGalponVaribale = this.obtenerLoteGalponDeHash(hashDatosGranja, granja, galpon);
                Integer edad = loteGalponVaribale.getEdad();

                String[] splitLoteCompleto = loteGalponVaribale.getLote().split("-");
                String opsap = splitLoteCompleto[1];
                String camaReciclada = "";
                if (splitLoteCompleto.length > 2) {
                    camaReciclada = splitLoteCompleto[2];
                    camaReciclada = camaReciclada.trim();
                }

                if (edad == 1) {
                    if (camaReciclada.equals("CRS")) {
                        datoTecnicoSap.setReciclaje("S");
                    } else {
                        datoTecnicoSap.setReciclaje("N");
                    }
                } else {
                    datoTecnicoSap.setReciclaje("");
                }

                HashMap<String, Object> hashAlimento = this.obtenerAlimentoXOpSap(opsap);
                Integer idEncaseta = (Integer) hashAlimento.get("idEncaseta");
                ArrayList<TablaAlimento> arAlimentos = (ArrayList<TablaAlimento>) hashAlimento.get("arAlimento");

                datoTecnicoSap.setIdEncaseta(idEncaseta);
                datoTecnicoSap.setFecha(cal.getTime());
                datoTecnicoSap.setGalpon(galpon);
                datoTecnicoSap.setDivision("1");
                datoTecnicoSap.setSeleccion(0);

                HashMap<String, ValidacionTolerancia> mapValidaciones = new HashMap<>();
                mapValidaciones.put("M", this.obtenerValidacion(arValidaciones, edad, "M"));
                mapValidaciones.put("H", this.obtenerValidacion(arValidaciones, edad, "H"));
                mapValidaciones.put("X", this.obtenerValidacion(arValidaciones, edad, "X"));
                //Validacion de Consumo
                this.realizarValidacionConsumo(datoTecnicoSap, arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones, arAlimentos, arDatosTecnicosExtras);
                //Validacion de Peso
                this.realizarValidacionPeso(datoTecnicoSap, arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones);
                //Validacion de Mortalidad
                this.realizarValidacionMortalidad(datoTecnicoSap, arLogEnvioSap, hashDatosGranja, loteGalponVaribale, mapValidaciones);

                if (datoTecnicoSap.getAplicaRegistro() && datoTecnicoSap.getIdEncaseta() != 0) {
                    arDatosTecnicosSap.add(datoTecnicoSap);
                    for (DatoTecnicoSap datoTecnicoExtra : arDatosTecnicosExtras) {
                        arDatosTecnicosSap.add(datoTecnicoExtra);
                    }
                }
            }
            ArrayList<RetornoDatosTecnicos> retornoDatosTecnicos = new ArrayList<>();
            if (!arDatosTecnicosSap.isEmpty()) {
                retornoDatosTecnicos = this.registrarDatosTecnicosSap(arDatosTecnicosSap);
                System.err.println("ESTO RETORNO SAPPP!!!!!!!!!!!!!!!!!------------");
                retornoDatosTecnicos.forEach(System.out::println);
                System.err.println("FIN DEL RETORNO DE SAPPP!!!!!!!!!!!!!!!!!------------");
            }
//            ArrayList<RetornoDatosTecnicos> retornoDatosTecnicos = new ArrayList<>();
//            retornoDatosTecnicos.add(new RetornoDatosTecnicos("E", "ZDA", 0, "Falta Peso Galpon: 3 Div: 1 Fecha: 20221210", "",
//                    0, "Falta Peso Galpon: 3 Div: 1 Fecha: 20221210", "", "", "", "", 0, "", ""));
//            retornoDatosTecnicos.add(new RetornoDatosTecnicos("S", "ZDA", 0, "Doc.Tecnico: 0003297638", "",
//                    0, "Doc.Tecnico: 0003297638", "", "", "", "", 0, "", ""));
            for (RetornoDatosTecnicos retornoDatosTecnico : retornoDatosTecnicos) {
                if (retornoDatosTecnico.getType().equals("S")) {
                    Pattern pattern = Pattern.compile("[0-9]{10}");
                    Matcher matcher = pattern.matcher(retornoDatosTecnico.getMessage());
                    String documento = "";
                    if (matcher.find()) {
                        documento = matcher.group();
                    }
                    String documentoEncontrado = documento;
                    if (retornoDatosTecnico.getMessage().contains("Doc:")) {
                        arLogEnvioSap.stream().filter(x -> x.getGranja().equals(granja) && x.getEstado()).forEach((logEnvio) -> {
                            logEnvio.setDocSapInventario(documentoEncontrado);
                        });
                    } else if (retornoDatosTecnico.getMessage().contains("Doc.Tecnico:")) {
                        arLogEnvioSap.stream().filter(x -> x.getGranja().equals(granja) && x.getEstado()).forEach((logEnvio) -> {
                            logEnvio.setDocSapTecnico(documentoEncontrado);
                        });
                    }
                } else {
                    System.err.println("Error retornado por SAP: " + retornoDatosTecnico.getMessage());
                }
            }

            System.out.println("Datos Tecnicos SAP");
            for (DatoTecnicoSap datoTecnicoSap : arDatosTecnicosSap) {
                System.out.println(datoTecnicoSap);
            }
        });

        System.out.println("Validacion total--------------------");
        System.out.println(arLogEnvioSap.size());
        arLogEnvioSap.stream().forEach(x -> System.out.println("Granja: " + x.getGranja() + ", Galpon: " + x.getGalpon()
                + ", estado: " + (x.getEstado() ? "True" : "False") + ", Variable:" + x.getVariable() + ", Observacion:" + x.getObservacion()));

        this.realizarEnvioMensajeSAP(arLogEnvioSap);
        return true;
    }

    public void realizarValidacionConsumo(DatoTecnicoSap datoTecnicoSap, ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
            LoteGalponVariable loteGalponVaribale, HashMap<String, ValidacionTolerancia> mapValidaciones, ArrayList<TablaAlimento> arAlimentos, ArrayList<DatoTecnicoSap> arDatosTecnicosExtras) {

        String granja = loteGalponVaribale.getCodGranja();
        String galpon = loteGalponVaribale.getGalpon();
        Integer edad = loteGalponVaribale.getEdad();

        ValidacionTolerancia validacionMacho = mapValidaciones.get("M");
        ValidacionTolerancia validacionHembra = mapValidaciones.get("H");
        ValidacionTolerancia validacionMixto = mapValidaciones.get("X");

        String siloName = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "SiloName1");
        String feedTotalPerDay = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedTotalPerDay");
        String feedPerAnimalPerDay = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "FeedPerAnimalPerDay");
        String broilerGroupTotalStockedFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedFemale");
        String broilerGroupTotalStockedMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedMale");

        Double feedTotalPerDayDouble = Objects.isNull(feedTotalPerDay) ? 0.0 : Double.valueOf(feedTotalPerDay);
        Double feedPerAnimalPerDayDouble = Objects.isNull(feedPerAnimalPerDay) ? 0.0 : Double.valueOf(feedPerAnimalPerDay);

        Double broilerGroupTotalStockedFemaleDouble = Objects.isNull(broilerGroupTotalStockedFemale) ? 0.0 : Double.valueOf(broilerGroupTotalStockedFemale);
        Double broilerGroupTotalStockedMaleDouble = Objects.isNull(broilerGroupTotalStockedMale) ? 0.0 : Double.valueOf(broilerGroupTotalStockedMale);

        String encasetamiento = "";
        if (broilerGroupTotalStockedFemaleDouble > 0 && broilerGroupTotalStockedMaleDouble > 0) {
            encasetamiento = "X";
        } else if (broilerGroupTotalStockedFemaleDouble > 0) {
            encasetamiento = "H";
        } else if (broilerGroupTotalStockedMaleDouble > 0) {
            encasetamiento = "M";
        }

        Boolean existeError = false;
        //Valido si alguno de los "3" valores no viene genero log por ausencia de variables
        if (Objects.isNull(siloName) || feedTotalPerDayDouble == 0 || feedPerAnimalPerDayDouble == 0) {
            if (Objects.isNull(siloName)) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "SiloName1", "Ausencia de variable", "", "", false);
            }
            if (feedTotalPerDayDouble == 0) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedTotalPerDay", "Ausencia de variable", "", "", false);
            }
            if (feedPerAnimalPerDayDouble == 0) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay",
                        "Ausencia de variable", "", "", false);
            }
            existeError = true;
        } else {
            if (feedPerAnimalPerDayDouble > 0) {
                if (encasetamiento.equals("X")) {
                    if (feedPerAnimalPerDayDouble < validacionMixto.getConsumoMinimo() || feedPerAnimalPerDayDouble > validacionMixto.getConsumoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay",
                                "El Consumo " + feedPerAnimalPerDayDouble + " con encasetamiento mixto para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                        existeError = true;
                    } else {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay", "El consumo para encasetamiento Mixto cumple con la tolerancia", "", "", true);
                    }
                } else if (encasetamiento.equals("H")) {
                    if (feedPerAnimalPerDayDouble < validacionHembra.getConsumoMinimo() || feedPerAnimalPerDayDouble > validacionHembra.getConsumoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay",
                                "El Consumo " + feedPerAnimalPerDayDouble + " con encasetamiento Hembra para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                        existeError = true;
                    } else {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay", "El consumo para encasetamiento Hembra cumple con la tolerancia", "", "", true);
                    }
                } else if (encasetamiento.equals("M")) {
                    if (feedPerAnimalPerDayDouble < validacionMacho.getConsumoMinimo() || feedPerAnimalPerDayDouble > validacionMacho.getConsumoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay",
                                "El Consumo " + feedPerAnimalPerDayDouble + " con encasetamiento Macho para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                        existeError = true;
                    } else {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay", "El consumo para encasetamiento Macho cumple con la tolerancia", "", "", true);
                    }
                } else {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedPerAnimalPerDay",
                            "No se logro obtener el sexo de encasetamiento para realizar la comparativa de tolerancia. Variables: BroilerGroupTotalStockedFemale y BroilerGroupTotalStockedMale.", "", "", false);
                    existeError = true;
                }
            }
            //Validacion SiloName
            List<TablaAlimento> arAlimentosSilo = arAlimentos.stream().filter(x -> x.getMaterial().equals(siloName)).collect(Collectors.toList());
            if (arAlimentosSilo.isEmpty()) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "SiloName1",
                        "No se encontro alimento asociado al material " + siloName, "", "", false);
            } else {
                Double cantidadMaterial = arAlimentosSilo.stream().mapToDouble(x -> x.getInventario()).sum();
                if (cantidadMaterial < feedTotalPerDayDouble) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedTotalPerDay",
                            "El consumo total por dia: " + feedTotalPerDayDouble + " es mayor que el inventario: " + cantidadMaterial.toString(), "", "", false);
                } else {
                    if (!existeError) {
                        datoTecnicoSap.setMaterial(siloName);
                        datoTecnicoSap.setAplicaRegistro(true);

                        Double alimentoRestante = feedTotalPerDayDouble;
                        for (int i = 0; i < arAlimentosSilo.size() && alimentoRestante > 0; i++) {
                            TablaAlimento alimento = arAlimentosSilo.get(i);

                            DatoTecnicoSap datoTecnicoAlimento = new DatoTecnicoSap();
                            datoTecnicoAlimento.setIdEncaseta(datoTecnicoSap.getIdEncaseta());
                            datoTecnicoAlimento.setFecha(datoTecnicoSap.getFecha());
                            datoTecnicoAlimento.setAplicaRegistro(true);
                            datoTecnicoAlimento.setGalpon(datoTecnicoSap.getGalpon());
                            datoTecnicoAlimento.setDivision(datoTecnicoSap.getDivision());
                            datoTecnicoAlimento.setSeleccion(datoTecnicoSap.getSeleccion());
                            datoTecnicoAlimento.setMaterial(datoTecnicoSap.getMaterial());
                            if (alimento.getInventario() >= alimentoRestante) {
                                if (i == 0) {
                                    datoTecnicoSap.setConsumo(alimentoRestante * 1000);
                                    datoTecnicoSap.setCharg(alimento.getLoteCompleto());
                                } else {
                                    datoTecnicoAlimento.setConsumo(alimentoRestante * 1000);
                                    datoTecnicoAlimento.setCharg(alimento.getLoteCompleto());
                                    arDatosTecnicosExtras.add(datoTecnicoAlimento);
                                }
                                alimentoRestante = 0.0;
                            } else {
                                alimentoRestante = alimentoRestante - alimento.getInventario();
                                if (i == 0) {
                                    datoTecnicoSap.setConsumo(alimento.getInventario() * 1000);
                                    datoTecnicoSap.setCharg(alimento.getLoteCompleto());
                                } else {
                                    datoTecnicoAlimento.setConsumo(alimento.getInventario() * 1000);
                                    datoTecnicoAlimento.setCharg(alimento.getLoteCompleto());
                                    arDatosTecnicosExtras.add(datoTecnicoAlimento);
                                }
                            }
                        }

                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "FeedTotalPerDay",
                                "Se registra consumo total por dia: " + feedTotalPerDayDouble + " para la edad " + edad, "", "", true);
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "SiloName1",
                                "Se registra material de consumo: " + siloName, "", "", true);
                    }
                }
            }
        }
    }

    public void realizarValidacionPeso(DatoTecnicoSap datoTecnicoSap, ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
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
        String broilerGroupTotalStockedFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedFemale");
        String broilerGroupTotalStockedMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupTotalStockedMale");

        Double birdScaleWeight1Double = (Objects.isNull(birdScaleWeight1)) ? 0.0 : Double.valueOf(birdScaleWeight1);
        Double birdScaleWeight2Double = (Objects.isNull(birdScaleWeight2)) ? 0.0 : Double.valueOf(birdScaleWeight2);
        Double broilerGroupWeightFemaleDouble = (Objects.isNull(broilerGroupWeightFemale)) ? 0.0 : Double.valueOf(broilerGroupWeightFemale);
        Double broilerGroupWeightMaleDouble = (Objects.isNull(broilerGroupWeightMale)) ? 0.0 : Double.valueOf(broilerGroupWeightMale);
        Double broilerGroupTotalStockedFemaleDouble = Objects.isNull(broilerGroupTotalStockedFemale) ? 0.0 : Double.valueOf(broilerGroupTotalStockedFemale);
        Double broilerGroupTotalStockedMaleDouble = Objects.isNull(broilerGroupTotalStockedMale) ? 0.0 : Double.valueOf(broilerGroupTotalStockedMale);

        String encasetamiento = "";
        if (broilerGroupTotalStockedFemaleDouble > 0 && broilerGroupTotalStockedMaleDouble > 0) {
            encasetamiento = "X";
        } else if (broilerGroupTotalStockedFemaleDouble > 0) {
            encasetamiento = "H";
        } else if (broilerGroupTotalStockedMaleDouble > 0) {
            encasetamiento = "M";
        }

        if (birdScaleWeight1Double == 0 && birdScaleWeight2Double == 0 && broilerGroupWeightFemaleDouble == 0 && broilerGroupWeightMaleDouble == 0) {
            this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2|BroilerGroupWeightFemale|BroilerGroupWeightMale",
                    "Ausencia de variable", "", "", false);
        } else if (birdScaleWeight1Double > 0 || birdScaleWeight2Double > 0) {
            int cantidadValores = (birdScaleWeight1Double > 0 && birdScaleWeight2Double > 0) ? 2 : 1;

            Double totalWeigth = (birdScaleWeight1Double + birdScaleWeight2Double) / cantidadValores;
            if (encasetamiento.equals("X")) {
                if (totalWeigth < validacionMixto.getPesoMinimo() || totalWeigth > validacionMixto.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia Mixta", "", "", false);
                } else {
                    datoTecnicoSap.setPeso(totalWeigth.intValue());
                    datoTecnicoSap.setAplicaRegistro(true);
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "Se registra peso Mixto " + totalWeigth + " para la edad " + edad, "", "", true);
                }
            } else if (encasetamiento.equals("H")) {
                if (totalWeigth < validacionHembra.getPesoMinimo() || totalWeigth > validacionHembra.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia Hembra", "", "", false);
                } else {
                    datoTecnicoSap.setPeso(totalWeigth.intValue());
                    datoTecnicoSap.setAplicaRegistro(true);
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "Se registra peso Hembra " + totalWeigth + " para la edad " + edad, "", "", true);
                }
            } else if (encasetamiento.equals("M")) {
                if (totalWeigth < validacionMacho.getPesoMinimo() || totalWeigth > validacionMacho.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia Macho", "", "", false);
                } else {
                    datoTecnicoSap.setPeso(totalWeigth.intValue());
                    datoTecnicoSap.setAplicaRegistro(true);
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                            "Se registra peso Macho " + totalWeigth + " para la edad " + edad, "", "", true);
                }
            } else {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdScaleWeight1|BirdScaleWeight2",
                        "No se logro obtener el sexo de encasetamiento para realizar la comparativa de tolerancia. Variables: BroilerGroupTotalStockedFemale y BroilerGroupTotalStockedMale.", "", "", false);
            }
        } else {
            Double totalWeigth;
            if (broilerGroupWeightFemaleDouble > 0 && broilerGroupWeightMaleDouble > 0) {
                totalWeigth = (broilerGroupWeightFemaleDouble + broilerGroupWeightMaleDouble) / 2;
                if (encasetamiento.equals("X")) {
                    if (totalWeigth < validacionMixto.getPesoMinimo() || totalWeigth > validacionMixto.getPesoMaximo()) {
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale|BroilerGroupWeightMale",
                                "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia Mixta", "", "", false);
                    } else {
                        datoTecnicoSap.setPeso(totalWeigth.intValue());
                        datoTecnicoSap.setAplicaRegistro(true);
                        this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale|BroilerGroupWeightMale",
                                "Se registra peso Mixto " + totalWeigth + " para la edad " + edad, "", "", true);
                    }
                }
            } else if (broilerGroupWeightFemaleDouble > 0) {
                totalWeigth = broilerGroupWeightFemaleDouble;
                if (totalWeigth < validacionHembra.getPesoMinimo() || totalWeigth > validacionHembra.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    datoTecnicoSap.setPeso(totalWeigth.intValue());
                    datoTecnicoSap.setAplicaRegistro(true);
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale",
                            "Se registra peso Hembra " + totalWeigth + " para la edad " + edad, "", "", true);
                }
            } else if (broilerGroupWeightMaleDouble > 0) {
                totalWeigth = broilerGroupWeightMaleDouble;
                if (totalWeigth < validacionMacho.getPesoMinimo() || totalWeigth > validacionMacho.getPesoMaximo()) {
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightMale",
                            "El Peso " + totalWeigth + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
                } else {
                    datoTecnicoSap.setPeso(totalWeigth.intValue());
                    datoTecnicoSap.setAplicaRegistro(true);
                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightMale",
                            "Se registra peso Macho " + totalWeigth + " para la edad " + edad, "", "", true);
                }
            } else if (broilerGroupWeightFemaleDouble == 0 && broilerGroupWeightMaleDouble == 0) {
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupWeightFemale|BroilerGroupWeightMale",
                        "Se obtuvo valores 0 para el registro de Peso", "", "", false);
            }

        }
    }

    public void realizarValidacionMortalidad(DatoTecnicoSap datoTecnicoSap, ArrayList<LogEnvioSap> arLogEnvioSap, HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja,
            LoteGalponVariable loteGalponVaribale, HashMap<String, ValidacionTolerancia> mapValidaciones) {

        String granja = loteGalponVaribale.getCodGranja();
        String galpon = loteGalponVaribale.getGalpon();
        Integer edad = loteGalponVaribale.getEdad();

//        ValidacionTolerancia validacionMacho = mapValidaciones.get("M");
//        ValidacionTolerancia validacionHembra = mapValidaciones.get("H");
//        ValidacionTolerancia validacionMixto = mapValidaciones.get("X");
        String broilerGroupMortalityFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupMortalityFemale");
        String broilerGroupMortalityMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BroilerGroupMortalityMale");
        String birdsDeadCulledPerDayFemale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BirdsDeadCulledPerDayFemale");
        String birdsDeadCulledPerDayMale = this.obtenerVariableDeHash(hashDatosGranja, granja, galpon, "BirdsDeadCulledPerDayMale");

        Double broilerGroupMortalityFemaleDouble = (Objects.isNull(broilerGroupMortalityFemale) ? 0.0 : Double.valueOf(broilerGroupMortalityFemale));
        Double broilerGroupMortalityMaleDouble = (Objects.isNull(broilerGroupMortalityMale) ? 0.0 : Double.valueOf(broilerGroupMortalityMale));
        Double birdsDeadCulledPerDayFemaleDouble = (Objects.isNull(birdsDeadCulledPerDayFemale) ? 0.0 : Double.valueOf(birdsDeadCulledPerDayFemale));
        Double birdsDeadCulledPerDayMaleDouble = (Objects.isNull(birdsDeadCulledPerDayMale) ? 0.0 : Double.valueOf(birdsDeadCulledPerDayMale));

//        if (Objects.isNull(broilerGroupMortalityFemale) && Objects.isNull(broilerGroupMortalityMale) && Objects.isNull(birdsDeadCulledPerDayFemale) && Objects.isNull(birdsDeadCulledPerDayMale)) {
        if (Objects.isNull(birdsDeadCulledPerDayFemale) && Objects.isNull(birdsDeadCulledPerDayMale)) {
            this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdsDeadCulledPerDayFemale|BirdsDeadCulledPerDayMale",
                    "Ausencia de variable", "", "", false);
        } else {
//            if ((Objects.isNull(broilerGroupMortalityFemale) || Objects.isNull(birdsDeadCulledPerDayFemale)) && (Objects.isNull(broilerGroupMortalityMale) || Objects.isNull(birdsDeadCulledPerDayMale))) {
//            if (Objects.isNull(birdsDeadCulledPerDayFemale) && Objects.isNull(birdsDeadCulledPerDayMale)) {
//                ArrayList<String> variablesAusentes = new ArrayList<>();
//                if (Objects.isNull(broilerGroupMortalityFemale)) {
//                    variablesAusentes.add("BroilerGroupMortalityFemale");
//                }
//                if (Objects.isNull(birdsDeadCulledPerDayFemale)) {
//                    variablesAusentes.add("BirdsDeadCulledPerDayFemale");
//                }
//                if (Objects.isNull(broilerGroupMortalityMale)) {
//                    variablesAusentes.add("BroilerGroupMortalityMale");
//                }
//                if (Objects.isNull(birdsDeadCulledPerDayMale)) {
//                    variablesAusentes.add("BirdsDeadCulledPerDayMale");
//                }
//                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, String.join("|", variablesAusentes),
//                        "Ausencia de variable", "", "", false);
//            } else {
            if (!Objects.isNull(birdsDeadCulledPerDayFemale) && !Objects.isNull(birdsDeadCulledPerDayMale)) {
                Double mortalidadMixto = (broilerGroupMortalityFemaleDouble + broilerGroupMortalityMaleDouble) / 2;
//                if (mortalidadMixto < validacionMixto.getMortalidadMinima() || mortalidadMixto > validacionMixto.getMortalidadMaxima()) {
//                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale|BirdsDeadCulledPerDayFemale|BroilerGroupMortalityMale|BirdsDeadCulledPerDayMale",
//                            "La Mortalidad Mixta " + mortalidadMixto + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
//                } else {
                Double birdsDeadCulledPerDayMixtoDouble = birdsDeadCulledPerDayFemaleDouble + birdsDeadCulledPerDayMaleDouble;

                datoTecnicoSap.setMortalidad(birdsDeadCulledPerDayMixtoDouble.intValue());
                datoTecnicoSap.setAplicaRegistro(true);
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdsDeadCulledPerDayFemale|BirdsDeadCulledPerDayMale",
                        "Se registra mortalidad Mixta " + mortalidadMixto + " para la edad " + edad, "", "", true);
//                }
            } else if (!Objects.isNull(birdsDeadCulledPerDayFemale)) {
//                if (broilerGroupMortalityFemaleDouble < validacionHembra.getMortalidadMinima() || broilerGroupMortalityFemaleDouble > validacionHembra.getMortalidadMaxima()) {
//                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityFemale",
//                        "La Mortalidad Hembra " + broilerGroupMortalityFemale + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
//                } else {
                datoTecnicoSap.setMortalidad(birdsDeadCulledPerDayFemaleDouble.intValue());
                datoTecnicoSap.setAplicaRegistro(true);
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdsDeadCulledPerDayFemale",
                        "Se registra mortalidad Hembra: " + broilerGroupMortalityFemale + " para la edad " + edad, "", "", true);
//                }
            } else if (!Objects.isNull(birdsDeadCulledPerDayMale)) {
//                if (broilerGroupMortalityMaleDouble < validacionMacho.getMortalidadMinima() || broilerGroupMortalityMaleDouble > validacionMacho.getMortalidadMaxima()) {
//                    this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BroilerGroupMortalityMale",
//                            "La Mortalidad Macho " + broilerGroupMortalityMaleDouble + " para la edad " + edad + " no cumple con la tabla de Tolerancia", "", "", false);
//                } else {
                datoTecnicoSap.setMortalidad(birdsDeadCulledPerDayMaleDouble.intValue());
                datoTecnicoSap.setAplicaRegistro(true);
                this.registrarLogEnvioSap(arLogEnvioSap, loteGalponVaribale, "BirdsDeadCulledPerDayMale",
                        "Se registra mortalidad Macho " + broilerGroupMortalityMaleDouble + " para la edad " + edad, "", "", true);
//                }
            }
        }
//        }
    }

    public String obtenerVariableDeHash(HashMap<String, ArrayList<LoteGalponVariable>> hashDatosGranja, String granja, String galpon, String variable) {
        LoteGalponVariable loteGalponVariable = hashDatosGranja.get(granja).stream()
                .filter(x -> {
                    return (x.getGalpon().equals(galpon) && x.getVariable().equals(variable));
                }).findFirst().orElse(null);
        if (Objects.isNull(loteGalponVariable)) {
            return null;
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
        arLogEnvioSap.add(new LogEnvioSap(loteGalponVariable.getGranja(), loteGalponVariable.getCodGranja(),
                loteGalponVariable.getGalpon(), loteGalponVariable.getLote(), loteGalponVariable.getEdad(), variable,
                new Date(), msg, docSapTecnico, docSapInventario, estado));
    }

    public HashMap<String, Object> obtenerAlimentoXOpSap(String opsap) {
        //PARA PRUEBAS NO MAS
        if (opsap.equals("000170023397")) {
            //Dardanelos 2
            opsap = "000170023021";
        } else if (opsap.equals("000170024000")) {
            //Dardanelos 1 
            opsap = "000170023022";
        } else if (opsap.equals("000170024140")) {
            //Santa clara 3
            opsap = "000170023023";
        } else {
            opsap = "000170023023";
        }
//        }
        //FIN DE PRUEBAS -------
        ArrayList<TablaAlimento> arTablaAlimentos = new ArrayList<>();
        Integer idEncaseta = 0;

        HashMap<String, Object> hashRespuesta = new HashMap<>();
        try {
//            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP");
            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP_calidad");
            JCoFunction funcion = destination.getRepository().getFunction("ZGWS_ALIMENTO");
            funcion.getImportParameterList().setValue("I_OP", opsap);
            funcion.execute(destination);

            JCoTable datos = funcion.getTableParameterList().getTable("T_ALIMENTO");
            for (int i = 0; i < datos.getNumRows(); i++) {
                datos.setRow(i);
                String matnr = datos.getString("MATERIAL");
                matnr = matnr.replaceFirst("^0+(?!$)", "");
                arTablaAlimentos.add(new TablaAlimento(matnr, datos.getString("DESCRIPCION"),
                        datos.getString("LOTE"), datos.getString("LOTECOMPLETO"),
                        datos.getDouble("INVENTARIO"), datos.getString("BULTO")));
            }

            idEncaseta = funcion.getExportParameterList().getInt("E_IDENCASETA");
            hashRespuesta.put("idEncaseta", idEncaseta);
            hashRespuesta.put("arAlimento", arTablaAlimentos);
        } catch (JCoException e) {
            hashRespuesta.put("idEncaseta", idEncaseta);
            hashRespuesta.put("arAlimento", new ArrayList<>());
            e.printStackTrace(System.err);
        }
        return hashRespuesta;
    }

    public ArrayList<RetornoDatosTecnicos> registrarDatosTecnicosSap(ArrayList<DatoTecnicoSap> arDatosTecnicos) {
        ArrayList<RetornoDatosTecnicos> arRetornoDatosTecnicos = new ArrayList<>();
        try {
//            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP");
            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP_calidad");
            JCoFunction funcion = destination.getRepository().getFunction("ZBPGR_BUSQ_DATOSTECNICOS");

            funcion.getImportParameterList().setValue("P_APP", "X");
            funcion.getImportParameterList().setValue("P_USUARIO", "TECNIFICADA");

            JCoTable datos = funcion.getTableParameterList().getTable("ZST_DATOS");
            for (DatoTecnicoSap datoTecnico : arDatosTecnicos) {
                datos.appendRow();
                datos.setValue("FECHA", datoTecnico.getFecha());
                datos.setValue("ID_ENCASETA", datoTecnico.getIdEncaseta());
                datos.setValue("GALPON", datoTecnico.getGalpon());
                datos.setValue("DIVISION", datoTecnico.getDivision());
                datos.setValue("MATERIAL", datoTecnico.getMaterial());
                datos.setValue("CONSUMO", datoTecnico.getConsumo());
                datos.setValue("CHARG", datoTecnico.getCharg());
                datos.setValue("MORTALIDAD", datoTecnico.getMortalidad());
                datos.setValue("SELECCION", datoTecnico.getSeleccion());
                datos.setValue("PESO", datoTecnico.getPeso());
                datos.setValue("CONSUMO_GAS", datoTecnico.getConsumoGas());
                datos.setValue("NOBSERVACION", datoTecnico.getNobservacion());
                datos.setValue("GALPONERO", datoTecnico.getGalponero());
                datos.setValue("RECICLAJE", datoTecnico.getReciclaje());
            }
            funcion.execute(destination);

            JCoTable tableReturn = funcion.getTableParameterList().getTable("IT_RETURN");
            for (int i = 0; i < tableReturn.getNumRows(); i++) {
                tableReturn.setRow(i);
                arRetornoDatosTecnicos.add(new RetornoDatosTecnicos(tableReturn.getString("TYPE"), tableReturn.getString("ID"), tableReturn.getInt("NUMBER"),
                        tableReturn.getString("MESSAGE"), tableReturn.getString("LOG_NO"), tableReturn.getInt("LOG_MSG_NO"), tableReturn.getString("MESSAGE_V1"),
                        tableReturn.getString("MESSAGE_V2"), tableReturn.getString("MESSAGE_V3"), tableReturn.getString("MESSAGE_V4"), tableReturn.getString("PARAMETER"),
                        tableReturn.getInt("ROW"), tableReturn.getString("FIELD"), tableReturn.getString("SYSTEM")));
            }

        } catch (JCoException e) {
            e.printStackTrace(System.err);
        }
        return arRetornoDatosTecnicos;
    }

    public void realizarEnvioMensajeSAP(ArrayList<LogEnvioSap> arLogEnvioSap) throws SQLException {
        ArrayList<RetornoDatosTecnicos> arRetornoLog = new ArrayList<>();
        ArrayList<TblCorreosNotificacion> arCorreosNotificacion = new ArrayList<>();
        try ( PreparedStatement ps = conexion.prepareStatement("select * from tblcorreosnotificacion")) {
            ResultSet res = ps.executeQuery();
            while (res.next()) {
//                arCorreosNotificacion.add(new TblCorreosNotificacion(res.getString("correo"), res.getString("error")));
            }
        }
        arCorreosNotificacion.add(new TblCorreosNotificacion("jmunoz@macpollo.com", ""));
        arCorreosNotificacion.add(new TblCorreosNotificacion("lidertdprod@macpollo.com", ""));
        arCorreosNotificacion.add(new TblCorreosNotificacion("jmunoz@macpollo.com", "X"));
        arCorreosNotificacion.add(new TblCorreosNotificacion("lidertdprod@macpollo.com", "X"));

        HashMap<String, String> hashMapVariableSignificado = new HashMap();
        hashMapVariableSignificado.put("FeedTotalPerDay", "Alimentacion total por dia");
        hashMapVariableSignificado.put("SiloName1", "Silo");
        hashMapVariableSignificado.put("FeedPerAnimalPerDay", "Alimentacion por animal por dia");
        hashMapVariableSignificado.put("BirdScaleWeight1", "Peso de Aves 1");
        hashMapVariableSignificado.put("BirdScaleWeight2", "Peso de Aves 2");
        hashMapVariableSignificado.put("BroilerGroupWeightFemale", "Peso grupo aves hembra");
        hashMapVariableSignificado.put("BroilerGroupWeightMale", "Peso grupo aves macho");
        hashMapVariableSignificado.put("BroilerGroupMortalityFemale", "Porcentaje mortalidad Hembra");
        hashMapVariableSignificado.put("BroilerGroupMortalityMale", "Porcentaje mortalidad Macho");
        hashMapVariableSignificado.put("BroilerGroupTotalStockedFemale", "Stock encasetado aves hembra");
        hashMapVariableSignificado.put("BroilerGroupTotalStockedMale", "Stock encasetado aves macho");
        hashMapVariableSignificado.put("BirdsDeadCulledPerDayFemale", "Aves hembra muertas por dia");
        hashMapVariableSignificado.put("BirdsDeadCulledPerDayMale", "Aves macho muertas por dia");

        try {
//            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP");
            JCoDestination destination = JCoDestinationManager.getDestination("config/ConexionSAP_calidad");
            JCoFunction funcion = destination.getRepository().getFunction("ZGWS_LOGGRANJASTECNI");

            funcion.getImportParameterList().setValue("P_APP", "X");
            funcion.getImportParameterList().setValue("P_USUARIO", "TECNIFICADA");

            JCoTable datosGranjas = funcion.getTableParameterList().getTable("T_LOGGRANJAS");
            //Añado los que generaron Error
            arLogEnvioSap.stream().filter(logEnvioSap -> !logEnvioSap.getEstado()).forEach(logEnvioSap -> {
                String[] splitVariables = logEnvioSap.getVariable().split("\\|");
                for (String variableEnvio : splitVariables) {
                    datosGranjas.appendRow();
                    datosGranjas.setValue("GRANJA", logEnvioSap.getGranja());
                    datosGranjas.setValue("NOMBRE_GRANJA", logEnvioSap.getGranjaCompleto());
                    datosGranjas.setValue("GALPON", logEnvioSap.getGalpon());
                    datosGranjas.setValue("CHARG", logEnvioSap.getLote());
                    datosGranjas.setValue("EDAD", logEnvioSap.getEdad());
                    datosGranjas.setValue("VARIAB_GRANJA", variableEnvio);
                    datosGranjas.setValue("VARIAB_TEXTO", hashMapVariableSignificado.get(variableEnvio));
                    datosGranjas.setValue("FECHA", logEnvioSap.getFecha());
                    datosGranjas.setValue("NOBSERVACION", logEnvioSap.getObservacion());
                    datosGranjas.setValue("DOCSAPTECNICO", logEnvioSap.getDocSapTecnico());
                    datosGranjas.setValue("DOCSAPINVENT", logEnvioSap.getDocSapInventario());
                    datosGranjas.setValue("ESTADO", logEnvioSap.getEstado() ? "X" : "");
                }
            });

            //Hago map para dejar solo los datos que necesito, luego filtro por Exitosos y los incluyo para enviar a SAP
            arLogEnvioSap.stream().map(x -> {
                return new LogEnvioSap(x.getGranjaCompleto(), x.getGranja(), x.getGalpon(), x.getLote(), x.getEdad(), "",
                        x.getFecha(), "", x.getDocSapTecnico(), x.getDocSapInventario(), x.getEstado());
            }).filter(x -> x.getEstado()).distinct().forEach(logEnvioSap -> {
                datosGranjas.appendRow();
                datosGranjas.setValue("GRANJA", logEnvioSap.getGranja());
                datosGranjas.setValue("NOMBRE_GRANJA", logEnvioSap.getGranjaCompleto());
                datosGranjas.setValue("GALPON", logEnvioSap.getGalpon());
                datosGranjas.setValue("CHARG", logEnvioSap.getLote());
                datosGranjas.setValue("EDAD", logEnvioSap.getEdad());
                datosGranjas.setValue("VARIAB_GRANJA", "");
                datosGranjas.setValue("FECHA", logEnvioSap.getFecha());
                datosGranjas.setValue("NOBSERVACION", logEnvioSap.getObservacion());
                datosGranjas.setValue("DOCSAPTECNICO", logEnvioSap.getDocSapTecnico());
                datosGranjas.setValue("DOCSAPINVENT", logEnvioSap.getDocSapInventario());
                datosGranjas.setValue("ESTADO", logEnvioSap.getEstado() ? "X" : "");
            });

            JCoTable tCorreos = funcion.getTableParameterList().getTable("T_CORREOSNOTI");
            for (TblCorreosNotificacion correoNotificacion : arCorreosNotificacion) {
                tCorreos.appendRow();
                tCorreos.setValue("EMAIL", correoNotificacion.getCorreo());
                tCorreos.setValue("ERROR", correoNotificacion.getError());
            }
            funcion.execute(destination);

            JCoTable tableReturn = funcion.getTableParameterList().getTable("IT_RETURN");
            for (int i = 0; i < tableReturn.getNumRows(); i++) {
                tableReturn.setRow(i);
                arRetornoLog.add(new RetornoDatosTecnicos(tableReturn.getString("TYPE"), tableReturn.getString("ID"), tableReturn.getInt("NUMBER"),
                        tableReturn.getString("MESSAGE"), tableReturn.getString("LOG_NO"), tableReturn.getInt("LOG_MSG_NO"), tableReturn.getString("MESSAGE_V1"),
                        tableReturn.getString("MESSAGE_V2"), tableReturn.getString("MESSAGE_V3"), tableReturn.getString("MESSAGE_V4"), tableReturn.getString("PARAMETER"),
                        tableReturn.getInt("ROW"), tableReturn.getString("FIELD"), tableReturn.getString("SYSTEM")));
            }
        } catch (JCoException e) {
            e.printStackTrace(System.err);
        }

        String msgConcatenado = arRetornoLog.stream().map(el -> el.getMessage()).collect(Collectors.joining("|"));
        arLogEnvioSap.forEach(x -> {
            x.setRespuestaenviosap(msgConcatenado);
            System.out.println(x);
            try {
                x.guardarObjeto(conexion);
            } catch (SQLException ex) {
                ex.printStackTrace(System.err);
            }
        });

    }

}
