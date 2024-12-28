package com.projet.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;


public class Tuile {

    private int x;
    private int y;
    private TuileType type;


    public Tuile(int x, int y, TuileType type) {
        this.x = x;
        this.y = y;
        this.type = type;

    }

    // Getters et setters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TuileType getType() {
        return type;
    }

    public void setType(TuileType type) {
        this.type = type;
    }


    protected static Tuile createTuile(int x, int y, TuileType tType) {
        return switch (tType) {
            case VILLE -> new Ville(x, y);
            case FORET -> new Foret(x, y);
            case MONTAGNE -> new Montagne(x, y);
            default -> new Tuile(x, y, tType);
        };
    }


}