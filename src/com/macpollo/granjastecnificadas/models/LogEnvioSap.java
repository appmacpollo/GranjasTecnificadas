/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Oficina
 */
public class LogEnvioSap {

    private final String tabla = "tbllogenviosap";

    private String granjaCompleto;
    private String granja;
    private String galpon;
    private String lote;
    private Integer edad;
    private String variable;
    private Date fecha;
    private String observacion;
    private String docSapTecnico;
    private String docSapInventario;
    private Boolean estado;
    private String respuestaenviosap;

    public LogEnvioSap() {
    }

    public LogEnvioSap(String granjaCompleto, String granja, String galpon, String lote, Integer edad, String variable, Date fecha, String observacion,
            String docSapTecnico, String docSapInventario, boolean estado) {
        this.granjaCompleto = granjaCompleto;
        this.granja = granja;
        this.galpon = galpon;
        this.lote = lote;
        this.edad = edad;
        this.variable = variable;
        this.fecha = fecha;
        this.observacion = observacion;
        this.docSapTecnico = docSapTecnico;
        this.docSapInventario = docSapInventario;
        this.estado = estado;
        this.respuestaenviosap = "";
    }

    public LogEnvioSap(String granjaCompleto, String granja, String galpon, String lote, Integer edad, String variable, Date fecha, String observacion,
            String docSapTecnico, String docSapInventario, boolean estado, String respuestaSap) {
        this.granjaCompleto = granjaCompleto;
        this.granja = granja;
        this.galpon = galpon;
        this.lote = lote;
        this.edad = edad;
        this.variable = variable;
        this.fecha = fecha;
        this.observacion = observacion;
        this.docSapTecnico = docSapTecnico;
        this.docSapInventario = docSapInventario;
        this.estado = estado;
        this.respuestaenviosap = respuestaSap;
    }

    public String getGranjaCompleto() {
        return granjaCompleto;
    }

    public void setGranjaCompleto(String granjaCompleto) {
        this.granjaCompleto = granjaCompleto;
    }

    public String getGranja() {
        return granja;
    }

    public void setGranja(String granja) {
        this.granja = granja;
    }

    public String getGalpon() {
        return galpon;
    }

    public void setGalpon(String galpon) {
        this.galpon = galpon;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getDocSapTecnico() {
        return docSapTecnico;
    }

    public void setDocSapTecnico(String docSapTecnico) {
        this.docSapTecnico = docSapTecnico;
    }

    public String getDocSapInventario() {
        return docSapInventario;
    }

    public void setDocSapInventario(String docSapInventario) {
        this.docSapInventario = docSapInventario;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getRespuestaenviosap() {
        return respuestaenviosap;
    }

    public void setRespuestaenviosap(String respuestaenviosap) {
        this.respuestaenviosap = respuestaenviosap;
    }

    public boolean guardarObjeto(Connection con) throws SQLException {
        boolean result = false;
        String[] arVariables = variable.split("\\|");
        for (String variableInsercion : arVariables) {
            try ( PreparedStatement ps = con.prepareStatement("insert into " + this.tabla + " (granja, galpon, lote, edad, variable, fecha, observacion, docsaptecnico, docsapinventario, respuestaenviosap) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, granja);
                ps.setString(2, galpon);
                ps.setString(3, lote);
                ps.setInt(4, edad);
                ps.setString(5, variableInsercion);
                ps.setTimestamp(6, new Timestamp(fecha.getTime()));
                ps.setString(7, observacion);
                ps.setString(8, docSapTecnico);
                ps.setString(9, docSapInventario);
                ps.setString(10, respuestaenviosap);

                result = ps.execute();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "LogEnvioSap{" + "tabla=" + tabla + ", granja=" + granja + ", galpon=" + galpon + ", lote=" + lote + ", edad=" + edad + ", variable=" + variable + ", fecha=" + fecha + ", observacion=" + observacion + ", docSapTecnico=" + docSapTecnico + ", docSapInventario=" + docSapInventario + ", estado=" + estado + ", respuestaenviosap=" + respuestaenviosap + '}';
    }

}
