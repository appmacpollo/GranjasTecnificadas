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
public class DatoTecnicoSap {

    private Date fecha;
    private Integer idEncaseta;
    private String galpon;
    private String division;
    private String material;
    private Double consumo;
    private String charg;
    private Integer mortalidad;
    private Integer seleccion;
    private Integer peso;
    private Double consumoGas;
    private String nobservacion;
    private String galponero;
    private String reciclaje;
    private Boolean aplicaRegistro;

    public DatoTecnicoSap() {
        this.idEncaseta = 0;
        this.galpon = "";
        this.division = "";
        this.material = "";
        this.consumo = 0.0;
        this.charg = "";
        this.mortalidad = 0;
        this.seleccion = 0;
        this.peso = 0;
        this.consumoGas = 0.0;
        this.nobservacion = "";
        this.galponero = "";
        this.reciclaje = "";
        this.aplicaRegistro = false;
    }

    public DatoTecnicoSap(Date fecha, Integer idEncaseta, String galpon, String division, String material, Double consumo, String charg, Integer mortalidad,
            Integer seleccion, Integer peso, Double consumoGas, String nobservacion, String galponero, String reciclaje, Boolean aplicaRegistro) {
        this.fecha = fecha;
        this.idEncaseta = idEncaseta;
        this.galpon = galpon;
        this.division = division;
        this.material = material;
        this.consumo = consumo;
        this.charg = charg;
        this.mortalidad = mortalidad;
        this.seleccion = seleccion;
        this.peso = peso;
        this.consumoGas = consumoGas;
        this.nobservacion = nobservacion;
        this.galponero = galponero;
        this.reciclaje = reciclaje;
        this.aplicaRegistro = aplicaRegistro;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getIdEncaseta() {
        return idEncaseta;
    }

    public void setIdEncaseta(Integer idEncaseta) {
        this.idEncaseta = idEncaseta;
    }

    public String getGalpon() {
        return galpon;
    }

    public void setGalpon(String galpon) {
        this.galpon = galpon;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
    }

    public String getCharg() {
        return charg;
    }

    public void setCharg(String charg) {
        this.charg = charg;
    }

    public Integer getMortalidad() {
        return mortalidad;
    }

    public void setMortalidad(Integer mortalidad) {
        this.mortalidad = mortalidad;
    }

    public Integer getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(Integer seleccion) {
        this.seleccion = seleccion;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public Double getConsumoGas() {
        return consumoGas;
    }

    public void setConsumoGas(Double consumoGas) {
        this.consumoGas = consumoGas;
    }

    public String getNobservacion() {
        return nobservacion;
    }

    public void setNobservacion(String nobservacion) {
        this.nobservacion = nobservacion;
    }

    public String getGalponero() {
        return galponero;
    }

    public void setGalponero(String galponero) {
        this.galponero = galponero;
    }

    public String getReciclaje() {
        return reciclaje;
    }

    public void setReciclaje(String reciclaje) {
        this.reciclaje = reciclaje;
    }

    public Boolean getAplicaRegistro() {
        return aplicaRegistro;
    }

    public void setAplicaRegistro(Boolean aplicaRegistro) {
        this.aplicaRegistro = aplicaRegistro;
    }

    @Override
    public String toString() {
        return "Fecha: " + fecha + ", idEncaseta: " + idEncaseta + ", Galpon: " + galpon + ", Division: " + division + ", Material: " + material + ", Consumo: " + consumo
                + ", Charg: " + charg + ", Mortalidad: " + mortalidad + ", Seleccion: " + seleccion + ", Peso: " + peso + ", ConsumoGas: " + consumoGas + ", Nobservacion: " + nobservacion
                + ", Galponero: " + galponero + ", Reciclaje: " + reciclaje + " aplicaRegistro: " + aplicaRegistro;
    }

}
