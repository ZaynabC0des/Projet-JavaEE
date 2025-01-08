package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ForetBDD;
import model.GameSession;
import model.VilleBDD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/session")
public class SessionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");


        if ("create".equals(action)) {
            // Créer une nouvelle session
            String sessionDetails = request.getParameter("details");
            String code = GameSession.createSession(sessionDetails);

            try {
                GameSession.generateRandomMap(code);
                response.getWriter().write("Session créée avec le code : " + code + " et map générée.");
            } catch (IOException e) {
                response.getWriter().write("Session créée avec le code : " + code + ", mais erreur lors de la génération de la map.");
            }
        } else if ("join".equals(action)) {
            // Rejoindre une session existante
            String code = request.getParameter("code");

            String sessionDetails = GameSession.joinSession(code);
            HttpSession session = request.getSession();
            session.setAttribute("code", code);

            if (sessionDetails != null) {
                // Chemin vers le dossier et le fichier CSV de l'utilisateur
               

                if (new File("H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\csv\\"+code+".csv").exists()) {
                    String gameFilePath = "H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\csv\\"+code+".csv";
                    session.setAttribute("gameFilePath", gameFilePath);
        
                    try {
                        // 1) Initialiser la grille
                        int[][] grille = initializeGrid(gameFilePath);
                        session.setAttribute("grille", grille);

                        // 2) Initialiser les villes dans la base de donn�es
                        VilleBDD villeBDD = new VilleBDD();
                        villeBDD.initializeCities(gameFilePath);

                        // Initialiser les for�ts dans la base de donn�es
                        ForetBDD foretBDD = new ForetBDD();
                        foretBDD.initializeTree(gameFilePath);

                        // Rediriger vers la page de lecture de la carte
                        response.sendRedirect("lecture_carte.jsp");

                    } catch (IOException e) {
                        System.out.println("Erreur lors de l'initialisation de la grille : " + e.getMessage());
                        request.setAttribute("error",
                                "Erreur lors de l'initialisation de la carte : " + e.getMessage());
                        request.getRequestDispatcher("connexion.jsp").forward(request, response);
                    } catch (SQLException e) {
                        System.out.println("Erreur lors de l'initialisation des villes dans la BDD : "
                                + e.getMessage());
                        request.setAttribute("error",
                                "Erreur lors de l'initialisation des villes dans la BDD : " + e.getMessage());
                        request.getRequestDispatcher("connexion.jsp").forward(request, response);
                    }

                } else {
                    request.setAttribute("error", "Fichier CSV non trouv�.");
                    request.getRequestDispatcher("connexion.jsp").forward(request, response);
                }
            } else {
                response.getWriter().write("Code de session invalide !");
            }
        } else {
            response.getWriter().write("Action invalide !");
        }
        
    }

    private int[][] initializeGrid(String filePath) throws IOException {
        List<int[]> tempGrid = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int[] row = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Integer.parseInt(values[i].trim());
                }
                tempGrid.add(row);
            }
        }
        // Convertir la liste temporaire en tableau 2D
        return tempGrid.toArray(new int[tempGrid.size()][]);
    }
}
