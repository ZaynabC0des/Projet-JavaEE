package model;

public class Combat {
    private int pointsDefenseCible;
    private String typeCible;
    private int lastDiceRoll; // Ajout pour stocker le dernier r�sultat du d�
    private String lastAttackMessage; // Ajout pour stocker le dernier message d'attaque

    public Combat(int pointsDefenseCible, String typeCible) {
        this.pointsDefenseCible = pointsDefenseCible;
        this.typeCible = typeCible;
    }

    public String Attaque(int pointsAttaque) {
        this.lastDiceRoll = pointsAttaque; // Mise � jour avec le r�sultat du d�
        pointsDefenseCible -= pointsAttaque;

        if (pointsDefenseCible < 0) {
            pointsDefenseCible = 0;
            this.lastAttackMessage = "Ville captur� ";
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
        return lastDiceRoll; // Renvoie le dernier r�sultat du d�
    }

    public String getLastAttackMessage() {
        return lastAttackMessage; // Renvoie le dernier message d'attaque
    }
}
