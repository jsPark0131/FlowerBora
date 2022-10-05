package com.example.flowerbora.Class;

import java.util.ArrayList;

public class Flower {
    private String name;
    private String floriography;// 꽃말
    private ArrayList<String> img_url;

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

    public ArrayList<String> getImg_url() {
        return img_url;
    }

    public void setImg_url(ArrayList<String> img_url) {
        this.img_url = img_url;
    }
}
