package com.android.linechart;

public class PointsModel {
    private String temperature;
    private String hours;

    public PointsModel() {
    }

    PointsModel(String hours, String temperature) {
        this.temperature = temperature;
        this.hours = hours;
    }

    String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
