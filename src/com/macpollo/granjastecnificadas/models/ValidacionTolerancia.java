/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

/**
 *
 * @author Oficina
 */
public class ValidacionTolerancia {

    private Integer edad;
    private String sexo;
    private Double consumoMinimo;
    private Double consumoMaximo;
    private Double pesoMinimo;
    private Double pesoMaximo;
    private Double mortalidadMinima;
    private Double mortalidadMaxima;

    public ValidacionTolerancia() {
    }

    public ValidacionTolerancia(Integer edad, String sexo, Double consumoMinimo, Double consumoMaximo, Double pesoMinimo, Double pesoMaximo, Double mortalidadMinima, Double mortalidadMaxima) {
        this.edad = edad;
        this.sexo = sexo;
        this.consumoMinimo = consumoMinimo;
        this.consumoMaximo = consumoMaximo;
        this.pesoMinimo = pesoMinimo;
        this.pesoMaximo = pesoMaximo;
        this.mortalidadMinima = mortalidadMinima;
        this.mortalidadMaxima = mortalidadMaxima;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Double getConsumoMinimo() {
        return consumoMinimo;
    }

    public void setConsumoMinimo(Double consumoMinimo) {
        this.consumoMinimo = consumoMinimo;
    }

    public Double getConsumoMaximo() {
        return consumoMaximo;
    }

    public void setConsumoMaximo(Double consumoMaximo) {
        this.consumoMaximo = consumoMaximo;
    }

    public Double getPesoMinimo() {
        return pesoMinimo;
    }

    public void setPesoMinimo(Double pesoMinimo) {
        this.pesoMinimo = pesoMinimo;
    }

    public Double getPesoMaximo() {
        return pesoMaximo;
    }

    public void setPesoMaximo(Double pesoMaximo) {
        this.pesoMaximo = pesoMaximo;
    }

    public Double getMortalidadMinima() {
        return mortalidadMinima;
    }

    public void setMortalidadMinima(Double mortalidadMinima) {
        this.mortalidadMinima = mortalidadMinima;
    }

    public Double getMortalidadMaxima() {
        return mortalidadMaxima;
    }

    public void setMortalidadMaxima(Double mortalidadMaxima) {
        this.mortalidadMaxima = mortalidadMaxima;
    }

    @Override
    public String toString() {
        return "Edad: " + this.getEdad() + ", Sexo: " + this.getSexo() + ", ConsumoMinimo: " + this.getConsumoMinimo() + ", ConsumoMaximo: " + this.getConsumoMaximo() + ", PesoMinimo: "
                + this.getPesoMinimo() + ", PesoMaximo: " + this.getPesoMaximo() + ", MortalidadMinima: " + this.getMortalidadMinima() + ", MortalidadMaxima: " + this.getMortalidadMaxima();
    }

}
