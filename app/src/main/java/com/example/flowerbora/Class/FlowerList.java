package com.example.flowerbora.Class;

import java.util.ArrayList;
import java.util.List;

public class FlowerList {

    private static FlowerList flowerList = new FlowerList();
    private List<Flower> flowers = new ArrayList<>();

    public static FlowerList getInstance() {
        return flowerList;
    }

    public List<Flower> getFlowers() {
        return flowers;
    }

    public void setFlowers(List<Flower> flowers) {
        this.flowers = flowers;
    }
}
