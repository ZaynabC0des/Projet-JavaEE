package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class Carte {

    private final Tuile[][] grille;
    private final ArrayList<Soldat> soldats;

    public Carte(String csvFilePath) throws IOException {
        this.soldats = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(Tuile.class.getResourceAsStream(csvFilePath))))) {

            ArrayList<String[]> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split(",");
                lines.add(values);
            }

            if (lines.isEmpty()) {
                throw new IOException("Le fichier CSV est vide ou introuvable: " + csvFilePath);
            }

            int rows = lines.size();
            int cols = lines.get(0).length;
            Tuile[][] map = new Tuile[rows][cols];

            for (int x = 0; x < rows; x++) {
                String[] rowValues = lines.get(x);
                for (int y = 0; y < cols; y++) {
                    String[] tileData = rowValues[y].trim().split(":");
                    int tileCode = Integer.parseInt(tileData[0]);
                    TuileType tType = TuileType.fromCode(tileCode);

                    if ( tileData.length == 3) {
                        String login = tileData[1];
                        int pointsDeVie = Integer.parseInt(tileData[2]);

                    } else {
                        map[x][y] = Tuile.createTuile(x, y, tType, "", 0); // Utiliser des valeurs par défaut pour login et points de vie
                    }
                }
            }

            this.grille = map;
        }
    }

    public Tuile getTuile(int x, int y) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            return grille[x][y];
        }
        return null;
    }

    public Tuile[][] getGrille() {
        return grille;
    }

    public ArrayList<Soldat> getSoldats() {
        return soldats;
    }

    public void setTuile(int x, int y, Tuile nouvelleTuile) {
        if (x >= 0 && x < grille.length && y >= 0 && y < grille[x].length) {
            grille[x][y] = nouvelleTuile;
        }
    }
}
