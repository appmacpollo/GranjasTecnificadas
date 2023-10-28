/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

/**
 *
 * @author Oficina
 */
public class TblCorreosNotificacion {

    private String correo;
    private String error;
    private Integer sociedad;

    public TblCorreosNotificacion() {
    }

    public TblCorreosNotificacion(String correo, String error, Integer sociedad) {
        this.correo = correo;
        this.error = error;
        this.sociedad = sociedad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getSociedad() {
        return sociedad;
    }

    public void setSociedad(Integer sociedad) {
        this.sociedad = sociedad;
    }

    @Override
    public String toString() {
        return "Correo => " + this.correo + ", Error => " + this.error + ", Sociedad => " + this.sociedad;
    }

}
