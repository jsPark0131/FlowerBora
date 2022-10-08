package com.example.flowerbora.Class;

import java.io.Serializable;

public class Flower implements Serializable {
    private String name;
    private String floriography;// 꽃말
    private String feature;// 특징
    private String period;
    private String Etc;

    public Flower() {
        this.name = "";
        this.floriography = "";
        this.feature = "";
        this.period = "";
        this.Etc = "";
    }

    public Flower(String name, String floriography, String feature, String period, String Etc) {
        this.name = name;
        this.floriography = floriography;
        this.feature = feature;
        this.period = period;
        this.Etc = Etc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloriography() {
        return floriography;
    }

    public void setFloriography(String floriography) {
        this.floriography = floriography;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getEtc() {
        return Etc;
    }

    public void setEtc(String etc) {
        Etc = etc;
    }
}
