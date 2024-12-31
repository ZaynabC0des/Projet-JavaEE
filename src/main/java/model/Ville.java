package model;

public class Ville extends Tuile {

    private int defensePoints;
    private String owner;
    private int productionPoints;


    public Ville(int x, int y) {
        super(x, y, TuileType.VILLE);

        this.defensePoints = 10;
        this.owner = null;  // J'ai pas la classe joueur encore
        this.productionPoints = 2;
    }

    // Getters et setters

    public int getDefensePoints() {
        return defensePoints;
    }

    public void setDefensePoints(int defensePoints) {
        this.defensePoints = defensePoints;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getProductionPoints() {
        return productionPoints;
    }

    public void setProductionPoints(int productionPoints) {
        this.productionPoints = productionPoints;
    }




}