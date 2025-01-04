package model;

public class Soldat extends Tuile {
    private int id_soldat;
    private String login_user;  // Utilisation du login au lieu de l'ID utilisateur
    private int pointsDeVie;

    // Constructeur
    public Soldat(int x, int y, String login_user, int pointsDeVie) {
        super(x, y, TuileType.SOLDAT); // Définit la position et le type
        this.login_user = login_user;
        this.pointsDeVie = pointsDeVie;
    }

    // Getters et setters
    public int getId() {
        return id_soldat;
    }

    public void setId(int id) {
        this.id_soldat = id;
    }

    public String getLoginUser() {
        return login_user;
    }

    public void setLoginUser(String login_user) {
        this.login_user = login_user;
    }

    public int getPointsDeVie() {
        return pointsDeVie;
    }

    public void setPointsDeVie(int pointsDeVie) {
        this.pointsDeVie = pointsDeVie;
    }
}
