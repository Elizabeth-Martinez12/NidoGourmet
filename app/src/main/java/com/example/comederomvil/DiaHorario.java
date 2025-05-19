package com.example.comederomvil;

public class DiaHorario {
    private String diaIng;
    private String diaEsp;
    private String startTime;
    private String endTime;

    public DiaHorario(String diaIng, String diaEsp, String startTime, String endTime) {
        this.diaIng = diaIng;
        this.diaEsp = diaEsp;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters y Setters
    public String getDiaIng() {
        return diaIng;
    }

    public String getDiaEsp() {
        return diaEsp;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}