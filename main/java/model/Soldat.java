package model;

public class Soldat {
    private int id;
    private int x;
    private int y;
    private int pointDeVie;
    private String imagePath;
    private String owner;

    // Constructeur par défaut
    public Soldat() {
    }

    // Constructeur complet
    public Soldat(int id, int x, int y, int pointDeVie, String imagePath) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.pointDeVie = pointDeVie;
        this.imagePath = imagePath;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPointDeVie() {
        return pointDeVie;
    }

    public void setPointDeVie(int pointDeVie) {
        this.pointDeVie = pointDeVie;
    }

    public void setDefensePoints(int pointsDeVie) {
	    this.pointDeVie = pointsDeVie;
	}


    public int setDefensePoints() {
        return pointDeVie;
    }
    
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
	public String getOwner() {
	    return owner;
	}
	
	public void setOwner(String owner) {
	    this.owner = owner;
	}
	
	

	
}
