/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

import java.util.Date;

/**
 *
 * @author Oficina
 */
public class LoteGalponVariable {

    private String codGranja;
    private String granja;
    private String galpon;
    private String lote;
    private Integer edad;
    private String variable;
    private String valor;
    private Date timestamp;

    public LoteGalponVariable() {
    }

    public LoteGalponVariable(String codGranja, String granja, String galpon, String lote, Integer edad, String variable, String valor, Date timestamp) {
        this.codGranja = codGranja;
        this.granja = granja;
        this.galpon = galpon;
        this.lote = lote;
        this.edad = edad;
        this.variable = variable;
        this.valor = valor;
        this.timestamp = timestamp;
    }

    public String getCodGranja() {
        return codGranja;
    }

    public void setCodGranja(String codGranja) {
        this.codGranja = codGranja;
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

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Granja: " + this.getGranja() + ",Galpon: " + this.getGalpon() + ",Lote: " + this.getLote() + ",Variable: " + this.getVariable() + ",Valor: " + this.getValor();
    }

}
