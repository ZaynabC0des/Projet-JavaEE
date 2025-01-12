package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.*;
import model.SoldatBDD;
import model.UserBDD;

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

            //String sessionDetails = GameSession.joinSession(code);
            HttpSession session = request.getSession();
            session.setAttribute("code", code);


            // Chemin vers le dossier et le fichier CSV de l'utilisateur

            
            if (new File("C:\\Users\\CYTech Student\\eclipse-workspace\\projet\\src\\main\\webapp\\csv\\" + code + ".csv").exists()) {
                String gameFilePath = "C:\\Users\\CYTech Student\\eclipse-workspace\\projet\\src\\main\\webapp\\csv\\" + code + ".csv";
                session.setAttribute("gameFilePath", gameFilePath);

                try {
                    // 1) Initialiser la grille
                    int[][] grille = initializeGrid(gameFilePath);
                    session.setAttribute("grille", grille);

                    // 2) Initialiser les villes dans la base de donn�es
                    VilleBDD villeBDD = new VilleBDD();
                    villeBDD.initializeCities(gameFilePath);

                    UserBDD userBDD = new UserBDD();
                    SoldatBDD soldatBDD = new SoldatBDD();
                    String userLogin = (String) session.getAttribute("userLogin");



                    if (grille == null) {
                        System.out.println("Erreur : Grille non initialisée.");
                        response.sendRedirect("lecture_carte.jsp");
                        return;
                    }

                    // Trouver toutes les cases vides
                    List<int[]> emptyPositions = new ArrayList<>();
                    for (int i = 0; i < grille.length; i++) {
                        for (int j = 0; j < grille[i].length; j++) {
                            if (grille[i][j] == 0) { // Case vide
                                emptyPositions.add(new int[]{i, j});
                            }
                        }
                    }


                    if (emptyPositions.isEmpty()) {
                        System.out.println("Erreur : Aucune case vide disponible pour positionner un soldat.");
                        response.sendRedirect("lecture_carte.jsp");
                        return;
                    }
                    Integer x = null;
                    Integer y = null;
                    for (int[] emptyPosition : emptyPositions) {
                        if (soldatBDD.existeSoldatPosition(emptyPosition[0], emptyPosition[1], (String) session.getAttribute("code"))) {
                            System.out.println("Erreur : Une autre entité occupe déjà la position (" + emptyPosition[0] + ", " + emptyPosition[1] + ").");

                        } else {
                            x = emptyPosition[0];
                            y = emptyPosition[1];
                            break;
                        }
                    }
                    if (x == null || y == null) {
                        System.out.println("Erreur : Aucune case vide disponible pour positionner un soldat.");
                        response.sendRedirect("lecture_carte.jsp");
                        return;
                    }
                    int soldatId = soldatBDD.ajouterSoldatEtRecupererId(userLogin, 100, x, y, (String) session.getAttribute("code"));

                    if (soldatId != -1) {
                        String json = String.format(
                            "{\"type\":\"move\",\"username\":\"%s\",\"soldatId\":%d,\"code\":\"%s\"}",
                            userLogin, soldatId,session.getAttribute("code")
                        );
                        GameWebSocket.broadcastMessage(json);

                        // Mettre à jour la grille
                        grille[x][y] = 4; // 4 représente un soldat
                        session.setAttribute("grille", grille);

                        // Déduire les points de production du joueur

                        int nombreSoldats = 0;
                        try {
                            nombreSoldats = userBDD.compterSoldatsPossedesParUtilisateur(userLogin, (String) session.getAttribute("code"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        session.setAttribute("nombreSoldats", nombreSoldats);

                        System.out.println("Soldat ajouté avec succès à la position (" + x + ", " + y + "), ID: " + soldatId);
                        response.sendRedirect("lecture_carte.jsp");}

                    } catch(IOException e){
                        System.out.println("Erreur lors de l'initialisation de la grille : " + e.getMessage());
                        request.setAttribute("error",
                                "Erreur lors de l'initialisation de la carte : " + e.getMessage());
                        request.getRequestDispatcher("connexion.jsp").forward(request, response);
                    } catch(SQLException e){
                        System.out.println("Erreur lors de l'initialisation des villes dans la BDD : "
                                + e.getMessage());
                        request.setAttribute("error",
                                "Erreur lors de l'initialisation des villes dans la BDD : " + e.getMessage());
                        request.getRequestDispatcher("connexion.jsp").forward(request, response);
                    }


            }
             else {
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
