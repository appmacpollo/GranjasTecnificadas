/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

/**
 *
 * @author oficina
 */
public class GranjaTecnificadaDTO {

    private String granja;
    private Integer sociedad;

    public GranjaTecnificadaDTO() {
    }

    public GranjaTecnificadaDTO(String granja, Integer sociedad) {
        this.granja = granja;
        this.sociedad = sociedad;
    }

    public String getGranja() {
        return granja;
    }

    public void setGranja(String granja) {
        this.granja = granja;
    }

    public Integer getSociedad() {
        return sociedad;
    }

    public void setSociedad(Integer sociedad) {
        this.sociedad = sociedad;
    }

    @Override
    public String toString() {
        return "GranjaTecnificadaDTO{" + "granja=" + granja + ", sociedad=" + sociedad + '}';
    }
    
    

}
