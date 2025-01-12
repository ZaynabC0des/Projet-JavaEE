
package model;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.security.SecureRandom;
import java.util.HashMap;

public class GameSession {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final HashMap<String, String> sessions = new HashMap<>(); // Code -> Session Details
    private static final SecureRandom random = new SecureRandom();

    // Génère un code alphanumérique unique
    public static String generateCode() {
        StringBuilder code;
        do {
            code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
        } while (sessions.containsKey(code.toString())); // Vérifie l'unicité
        return code.toString();
    }

    // Crée une session avec un code
    public static String createSession(String details) {
        String code = generateCode();
        sessions.put(code, details);
        return code;
    }

    // Rejoint une session via un code
    public static String joinSession(String code) {

        return sessions.getOrDefault(code, null);
    }

    // Vérifie si un code existe
    public static boolean sessionExists(String code) {
        return sessions.containsKey(code);
    }

    public static void generateRandomMap(String code) throws IOException {
        String filePath = "C:\\Users\\CYTech Student\\eclipse-workspace\\projet\\src\\main\\webapp\\csv\\" + code + ".csv";
        String DefaultPath = "C:\\Users\\CYTech Student\\eclipse-workspace\\projet\\src\\main\\webapp\\csv\\default.csv";
        try (FileWriter writer = new FileWriter(filePath); FileReader defaultReader = new FileReader(DefaultPath)) {
            //mettre la valeur de default.csv dans le fichier de la partie
            int c;
            while ((c = defaultReader.read()) != -1) {
                writer.write(c);
            }
        }


    }
}

/*
    public static void generateRandomMap(String code) throws IOException {
        String filePath = "H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\csv\\" + code + ".csv";
        int maxMountains = 8; // Nombre maximum de montagnes
        int mountainCount = 0; // Compteur pour les montagnes

        try (FileWriter writer = new FileWriter(filePath)) {
            // Générer un tableau 10x10 avec des valeurs aléatoires entre 0 et 3
            for (int row = 0; row < 10; row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < 10; col++) {
                    int value;

                    if (mountainCount < maxMountains) {
                        // Génère un nombre entre 0 et 3, mais limite les montagnes (3)
                        value = random.nextInt(4); // Nombre aléatoire entre 0 et 3
                        if (value == 3) {
                            mountainCount++;
                        }
                    } else {
                        // Une fois la limite atteinte, générer uniquement des nombres entre 0 et 2
                        value = random.nextInt(3); // Nombre aléatoire entre 0 et 2
                    }

                    line.append(value);
                    if (col < 9) {
                        line.append(","); // Ajouter une virgule entre les colonnes
                    }
                }
                writer.write(line.toString());
                writer.write("\n"); // Nouvelle ligne après chaque rangée
            }
        }
    }
*/


