package model;

public class Combat {
    private int pointsDefenseCible; // Points de défense de l'unité ou de la ville
    private String typeCible;       // "soldat" ou "ville"

    public Combat(int pointsDefenseCible, String typeCible) {
        this.pointsDefenseCible = pointsDefenseCible;
        this.typeCible = typeCible;
    }

    public String Attaque(int pointsAttaque) {
        pointsDefenseCible -= pointsAttaque;
        if (pointsDefenseCible <= 0) {
            pointsDefenseCible = 0; 
            if (typeCible.equals("ville")) {
                return "La ville a été capturée !";
            } else {
                return "L'unité ennemie a été vaincue, le soldat occupe désormais sa place.";
            }
        } else {
            return "Damage: " + pointsAttaque ;
        }
    }

    public boolean estCibleEnVie() {
        return pointsDefenseCible > 0;
    }

    public int getPointsDefenseCible() {
        return pointsDefenseCible;
    }

    public String getTypeCible() {
        return typeCible;
    }
}
