package com.projet.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class Carte {

    // Grille de tuiles
    private final Tuile[][] grille;


    public Carte(String csvFilePath) throws IOException {

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
                    int tileCode = Integer.parseInt(rowValues[y].trim());
                    TuileType tType = TuileType.fromCode(tileCode);
                    map[x][y] = Tuile.createTuile(x, y, tType);
                }
            }

            this.grille = map;
        }
    }



    public Tuile getTuile(int x, int y) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            return grille[x][y];
        }
        // Si la position est hors limites, on retourne null.
        return null;
    }

    /* On peut utiliser Tuile.createTuile pour modifier la tuile ou la créer.
    public void setTuile(int x, int y, Tuile nouvelleTuile) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            grille[x][y] = nouvelleTuile;
        }
    }
    */

}
