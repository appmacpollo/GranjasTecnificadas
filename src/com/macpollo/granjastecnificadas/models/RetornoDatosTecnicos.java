/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.macpollo.granjastecnificadas.models;

/**
 *
 * @author Oficina
 */
public class RetornoDatosTecnicos {

    private String type;
    private String id;
    private Integer number;
    private String message;
    private String logNo;
    private Integer logMsgNo;
    private String messageV1;
    private String messageV2;
    private String messageV3;
    private String messageV4;
    private String parameter;
    private Integer row;
    private String field;
    private String system;

    public RetornoDatosTecnicos() {
    }

    public RetornoDatosTecnicos(String type, String id, Integer number, String message, String logNo, Integer logMsgNo, String messageV1, String messageV2, String messageV3, String messageV4, String parameter, Integer row, String field, String system) {
        this.type = type;
        this.id = id;
        this.number = number;
        this.message = message;
        this.logNo = logNo;
        this.logMsgNo = logMsgNo;
        this.messageV1 = messageV1;
        this.messageV2 = messageV2;
        this.messageV3 = messageV3;
        this.messageV4 = messageV4;
        this.parameter = parameter;
        this.row = row;
        this.field = field;
        this.system = system;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLogNo() {
        return logNo;
    }

    public void setLogNo(String logNo) {
        this.logNo = logNo;
    }

    public Integer getLogMsgNo() {
        return logMsgNo;
    }

    public void setLogMsgNo(Integer logMsgNo) {
        this.logMsgNo = logMsgNo;
    }

    public String getMessageV1() {
        return messageV1;
    }

    public void setMessageV1(String messageV1) {
        this.messageV1 = messageV1;
    }

    public String getMessageV2() {
        return messageV2;
    }

    public void setMessageV2(String messageV2) {
        this.messageV2 = messageV2;
    }

    public String getMessageV3() {
        return messageV3;
    }

    public void setMessageV3(String messageV3) {
        this.messageV3 = messageV3;
    }

    public String getMessageV4() {
        return messageV4;
    }

    public void setMessageV4(String messageV4) {
        this.messageV4 = messageV4;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "RetornoDatosTecnicos{" + "type=" + type + ", id=" + id + ", number=" + number + ", message=" + message + ", logNo=" + logNo + ", logMsgNo=" + logMsgNo + ", messageV1=" + messageV1 + ", messageV2=" + messageV2 + ", messageV3=" + messageV3 + ", messageV4=" + messageV4 + ", parameter=" + parameter + ", row=" + row + ", field=" + field + ", system=" + system + '}';
    }

}
