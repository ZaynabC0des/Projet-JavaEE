package com.projet.model;

import static java.lang.Math.*;

public class Foret extends Tuile {


    private int resources;


    public Foret(int x, int y) {
        super(x, y, TuileType.FORET);

        this.resources = (int)(random()*100);
    }

    // Getters et setters

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }



}
