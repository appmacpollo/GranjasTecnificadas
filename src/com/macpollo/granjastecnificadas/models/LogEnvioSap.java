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

    public LogEnvioSap() {
    }

    public LogEnvioSap(String granja, String galpon, String lote, Integer edad, String variable, Date fecha, String observacion,
            String docSapTecnico, String docSapInventario, boolean estado) {
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

    public boolean guardarObjeto(Connection con) throws SQLException {
        boolean result = false;
        try ( PreparedStatement ps = con.prepareStatement("insert into " + this.tabla + " (granja, galpon, lote, edad, variable, fecha, observacion, docsaptecnico, docsapinventario) VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, granja);
            ps.setString(2, galpon);
            ps.setString(3, lote);
            ps.setInt(4, edad);
            ps.setString(5, variable);
            ps.setTimestamp(6, new Timestamp(fecha.getTime()));
            ps.setString(7, observacion);
            ps.setString(8, docSapTecnico);
            ps.setString(9, docSapInventario);

            result = ps.execute();
        }
        return result;
    }

}
