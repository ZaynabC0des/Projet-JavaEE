package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class Carte {

    // Grille de tuiles
    private final Tuile[][] grille;

    // Liste des soldats sur la carte
    private final ArrayList<Soldat> soldats;

    public Carte(String csvFilePath) throws IOException {
        this.soldats = new ArrayList<>(); // Initialisation de la liste des soldats

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(Tuile.class.getResourceAsStream(csvFilePath))))) {

            ArrayList<String[]> lines = new ArrayList<>();
            String line;

            // Lecture du fichier ligne par ligne
            while ((line = br.readLine()) != null) {
                // Séparation par virgules
                String[] values = line.trim().split(",");
                lines.add(values);
            }

            if (lines.isEmpty()) {
                throw new IOException("Le fichier CSV est vide ou introuvable: " + csvFilePath);
            }

            // Création du tableau 2D de tuiles
            int rows = lines.size();
            int cols = lines.get(0).length;
            Tuile[][] map = new Tuile[rows][cols];

            for (int x = 0; x < rows; x++) {
                String[] rowValues = lines.get(x);
                for (int y = 0; y < cols; y++) {
                    // Exemple : Code = 4 (soldat), suivi de propriétés séparées par des ":"
                    String[] tileData = rowValues[y].trim().split(":");
                    int tileCode = Integer.parseInt(tileData[0]);
                    TuileType tType = TuileType.fromCode(tileCode);

                    if (tType == TuileType.SOLDAT && tileData.length == 3) {
                        // Chargement des soldats avec ID_Joueur et Points de Vie
                        int idJoueur = Integer.parseInt(tileData[1]);
                        int pointsDeVie = Integer.parseInt(tileData[2]);

                        Soldat soldat = new Soldat(x, y, idJoueur, pointsDeVie);
                        soldats.add(soldat); // Ajouter le soldat à la liste
                        map[x][y] = soldat;
                    } else {
                        map[x][y] = Tuile.createTuile(x, y, tType, 0, 0); // Autres types de tuiles
                    }
                }
            }

            this.grille = map;
        }
    }

    // Récupérer une tuile de la carte
    public Tuile getTuile(int x, int y) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            return grille[x][y];
        }
        return null; // Retourne null si la position est hors limites
    }

    // Retourne la grille complète
    public Tuile[][] getGrille() {
        return grille;
    }

    // Retourne la liste des soldats
    public ArrayList<Soldat> getSoldats() {
        return soldats;
    }

    // Mettre à jour une tuile sur la carte
    public void setTuile(int x, int y, Tuile nouvelleTuile) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            grille[x][y] = nouvelleTuile;
        }
    }
}
