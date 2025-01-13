package model;

public class Combat {
    private int pointsDefenseCible;
    private String typeCible;
    private int lastDiceRoll; // Ajout pour stocker le dernier résultat du dé
    private String lastAttackMessage; // Ajout pour stocker le dernier message d'attaque

    public Combat(int pointsDefenseCible, String typeCible) {
        this.pointsDefenseCible = pointsDefenseCible;
        this.typeCible = typeCible;
    }

    public String Attaque(int pointsAttaque) {
        this.lastDiceRoll = pointsAttaque; // Mise à jour avec le résultat du dé
        pointsDefenseCible -= pointsAttaque;

        if (pointsDefenseCible < 0) {
            pointsDefenseCible = 0;
            this.lastAttackMessage = "Ville capturé ";
        } else {
            this.lastAttackMessage = "Damage: " + pointsAttaque;
        }
        
        return lastAttackMessage;
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

    public int getLastDiceRoll() {
        return lastDiceRoll; // Renvoie le dernier résultat du dé
    }

    public String getLastAttackMessage() {
        return lastAttackMessage; // Renvoie le dernier message d'attaque
    }
}
