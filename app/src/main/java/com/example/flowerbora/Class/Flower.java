package com.example.flowerbora.Class;

import java.io.Serializable;
import java.util.List;

public class Flower implements Serializable {
    private String name;
    private String floriography;// 꽃말
    private String feature;// 특징
    private String period;
    private String Etc;
    private List<Double> latlng_double;

    public Flower() {
        this.name = "";
        this.floriography = "";
        this.feature = "";
        this.period = "";
        this.Etc = "";
    }

    public Flower(String name, String floriography, String feature, String period, String Etc, List<Double> latlng_doube) {
        this.name = name;
        this.floriography = floriography;
        this.feature = feature;
        this.period = period;
        this.Etc = Etc;
        this.latlng_double = latlng_doube;
    }

    public List<Double> getLatlng_double() {
        return latlng_double;
    }

    public void setLatlng_double(List<Double> latlng_double) {
        this.latlng_double = latlng_double;
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
