/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

/**
 *
 * @author Oficina
 */
public class TablaAlimento {

    private String material;
    private String descripcion;
    private String lote;
    private String loteCompleto;
    private Double inventario;
    private String bulto;

    public TablaAlimento() {
    }

    public TablaAlimento(String material, String descripcion, String lote, String loteCompleto, Double inventario, String bulto) {
        this.material = material;
        this.descripcion = descripcion;
        this.lote = lote;
        this.loteCompleto = loteCompleto;
        this.inventario = inventario;
        this.bulto = bulto;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getLoteCompleto() {
        return loteCompleto;
    }

    public void setLoteCompleto(String loteCompleto) {
        this.loteCompleto = loteCompleto;
    }

    public Double getInventario() {
        return inventario;
    }

    public void setInventario(Double inventario) {
        this.inventario = inventario;
    }

    public String getBulto() {
        return bulto;
    }

    public void setBulto(String bulto) {
        this.bulto = bulto;
    }

    @Override
    public String toString() {
        return "Material: " + this.material + ", Lote: " + this.lote + ", loteCompleto: "
                + this.loteCompleto + ", Descripcion: " + this.descripcion + ", Inventario: " + this.inventario + ", Bulto: " + this.bulto;
    }

}
