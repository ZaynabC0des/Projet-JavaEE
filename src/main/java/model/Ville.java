package model;

public class Ville extends Tuile {

    private int defensePoints;
    private String owner;
    private int productionPoints;
    private String id;
    private int positionX;
    private int positionY;

    // Constructeur par dÃ©faut
    public Ville() {
        super(0, 0, TuileType.VILLE); // Position par dÃ©faut (0,0) et type VILLE
        this.defensePoints = 100;
        this.owner = null;  // Pas de propriÃ©taire par dÃ©faut
        this.productionPoints = 5;
        this.id = null; // ID par dÃ©faut
        this.positionX = 0;
        this.positionY = 0;
    }

    // Constructeur avec paramÃ¨tres
    public Ville(int x, int y) {
        super(x, y, TuileType.VILLE);
        this.defensePoints = 100;
        this.owner = null;
        this.productionPoints = 5;
        this.positionX = x;
        this.positionY = y;
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

    public boolean estCibleEnVie() {
        return defensePoints > 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
}