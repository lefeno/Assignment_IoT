package com.example.bill.assignmentiot;

public class Param {
    private boolean auto;
    private String id;
    private String value;
    private String name;
    private String type;

    public Param(){

    }

    public Param(boolean auto, String id, String value, String name, String type) {
        this.auto = auto;
        this.id = id;
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public boolean isAuto() {
        return auto;
    }

    public String getID() {
        return id;
    }

    public String getRawValue() {
        return value;
    }

    public String getValue() {
        return value + " %";
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }
}
