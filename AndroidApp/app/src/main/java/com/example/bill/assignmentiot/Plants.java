package com.example.bill.assignmentiot;

public class Plants {
    private Integer humid_min;
    private Integer humid_max;
    private Integer id;
    private String name;

    public Plants(){

    }

    public Plants(Integer humid_min, Integer humid_max, Integer id, String name) {
        this.humid_min = humid_min;
        this.humid_max = humid_max;
        this.id = id;
        this.name = name;
    }

    public Integer getHumid_min() {
        return humid_min;
    }

    public Integer getHumid_max() {
        return humid_max;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
