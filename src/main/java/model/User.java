package model;

public class User {
    private int id; // Identifiant unique de l'utilisateur
    private String login;
    private String password;
    private int point_production; // Points de production
    private String soldierImage; // Image des soldats attribuée à l'utilisateur
    
    // Constructeur avec ID
    public User(int id, String login, String password, int point_production) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.point_production = point_production;
        this.soldierImage = "default-soldier.png"; // Image par défaut
    }
    
    public User(int id, String login, String password, int point_production, String soldierImage) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.point_production = point_production;
        this.soldierImage = soldierImage;
    }


    // Constructeurs sans ID pour certaines utilisations
    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPointProduction() {
        return point_production;
    }

    public void setPointProduction(int point_production) {
        this.point_production = point_production;
    }
    public void addProductionPoints(int points) {
        this.point_production += points; // Ajoute les points à l'attribut existant
    }

    public String getSoldierImage() {
        return soldierImage;
    }

    public void setSoldierImage(String soldierImage) {
        this.soldierImage = soldierImage;
    }


}