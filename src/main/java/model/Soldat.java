package model;

public class Soldat extends Tuile {
    private int id_soldat;
    private int id_user;
    private int pointsDeVie;

    // Constructeur
    public Soldat(int x, int y, int id_user, int pointsDeVie) {
        super(x, y, TuileType.SOLDAT); // Définit la position et le type
        this.id_user = id_user;
        this.pointsDeVie = pointsDeVie;
    }

    // Getters et setters
    public int getId() {
        return id_soldat;
    }

    public void setId(int id) {
        this.id_soldat = id;
    }

    public int getid_user() {
        return id_user;
    }

    public void setid_user(int id_user) {
        this.id_user = id_user;
    }

    public int getPointsDeVie() {
        return pointsDeVie;
    }

    public void setPointsDeVie(int pointsDeVie) {
        this.pointsDeVie = pointsDeVie;
    }

    // Méthode pour infliger des dégâts au soldat
    public void takeDamage(int damage) {
        this.pointsDeVie -= damage;
        if (this.pointsDeVie < 0) {
            this.pointsDeVie = 0; // Santé minimale à 0
        }
    }

    // Méthode pour vérifier si le soldat est encore en vie
    public boolean isAlive() {
        return this.pointsDeVie > 0;
    }
}
