package model;

public class Soldat {
	private int id;
    private int x;
    private int y;
    private int pointsDeVie;
    private String loginUser;

    // Constructor matching the parameters in SoldatBDD
    public Soldat(int id, int x, int y, int pointsDeVie) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.pointsDeVie = pointsDeVie;
    }

    // Another constructor with the loginUser parameter
    public Soldat(int x, int y, String loginUser, int pointsDeVie) {
        this.x = x;
        this.y = y;
        this.loginUser = loginUser;
        this.pointsDeVie = pointsDeVie;
    }

    // Getters and setters
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

    public int getPointsDeVie() {
        return pointsDeVie;
    }

    public void setPointsDeVie(int pointsDeVie) {
        this.pointsDeVie = pointsDeVie;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }
}