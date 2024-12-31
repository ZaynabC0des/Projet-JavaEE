package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.SoldatBDD;
import model.User;
import model.UserBDD;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/RecruitSoldierServlet")
public class RecruitSoldierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null || user.getId() <= 0) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié ou ID utilisateur invalide.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        SoldatBDD soldatBDD = new SoldatBDD();
        int idSoldat = soldatBDD.ajouterSoldatEtRecupererId(user.getId(), 100); // 100 points de vie
		if (idSoldat > 0) {
		    System.out.println("Soldat ajouté avec succès ! ID Soldat : " + idSoldat);
		    session.setAttribute("message", "Soldat créé avec succès (ID : " + idSoldat + ").");
		} else {
		    System.out.println("Erreur lors de l'ajout du soldat.");
		    session.setAttribute("errorMessage", "Erreur lors de la création du soldat.");
		}

        response.sendRedirect("lecture_carte.jsp");
    }


    private int[] addSoldierToMap(int[][] grille, String csvFilePath) throws IOException {
        List<int[]> emptyPositions = new ArrayList<>();
        // Rechercher les cases vides
        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille[i].length; j++) {
                if (grille[i][j] == 0) { // Case vide
                    emptyPositions.add(new int[]{i, j});
                }
            }
        }

        // Vérifier qu'il y a des cases disponibles
        if (emptyPositions.isEmpty()) {
            return null;
        }

        // Sélectionner une position aléatoire
        int[] randomPosition = emptyPositions.get((int) (Math.random() * emptyPositions.size()));
        int x = randomPosition[0];
        int y = randomPosition[1];

        // Placer le soldat dans la grille
        grille[x][y] = 4; // Code 4 pour un soldat

        // Mettre à jour le fichier CSV
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        String[] row = lines.get(x).split(",");
        row[y] = "4"; // Mettre à jour la case avec le code du soldat
        lines.set(x, String.join(",", row));

        try (FileWriter writer = new FileWriter(new File(csvFilePath), false)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }

        return randomPosition;
    }
}
