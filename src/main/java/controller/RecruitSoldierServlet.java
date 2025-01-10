package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Soldat;
import model.SoldatBDD;
import model.UserBDD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@WebServlet("/RecruitSoldierServlet")
public class RecruitSoldierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userLogin = (String) session.getAttribute("userLogin");

        if (userLogin == null || userLogin.isEmpty()) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié ou le login est invalide.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        UserBDD userBDD = new UserBDD();
        SoldatBDD soldatBDD = new SoldatBDD();
        
        try {
            int currentPoints = userBDD.getProductionPoints(userLogin);
            if (currentPoints < 15) {
                System.out.println("Pas assez de points de production pour recruter un soldat.");
                session.setAttribute("errorMessage", "Il vous manque encore des points de production pour recruter un soldat.");
                response.sendRedirect("lecture_carte.jsp"); // Rediriger vers la page où le message doit être affiché
                return;
            }

            // Les points sont suffisants pour recruter un soldat
            session.setAttribute("canRecruit", true);

            int[][] grille = (int[][]) session.getAttribute("grille");
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

            // Choisir une position aléatoire
            Random random = new Random();
            int[] chosenPosition = emptyPositions.get(random.nextInt(emptyPositions.size()));
            int x = chosenPosition[0];
            int y = chosenPosition[1];

            // Vérifier que la position est toujours vide dans la base de données
            if (soldatBDD.existeSoldatPosition(x, y)) {
                System.out.println("Erreur : Une autre entité occupe déjà la position (" + x + ", " + y + ").");
                response.sendRedirect("lecture_carte.jsp");
                return;
            }

            // Ajouter le soldat à la base de données avec ses coordonnées
            int soldatId = soldatBDD.ajouterSoldatEtRecupererId(userLogin, 100, x, y);
            if (soldatId != -1) {
                // Mettre à jour la grille
                grille[x][y] = 4; // 4 représente un soldat
                session.setAttribute("grille", grille);

                // Déduire les points de production du joueur
                userBDD.updateProductionPoints(userLogin, -15);
                userBDD.updateScore(userLogin, +5);
                int nombreSoldats = userBDD.compterSoldatsPossedesParUtilisateur(userLogin);
                int score = userBDD.getUserScore(userLogin);
                session.setAttribute("nombreSoldats", nombreSoldats);
                session.setAttribute("score", score);
                System.out.println("Soldat ajouté avec succès à la position (" + x + ", " + y + "), ID: " + soldatId);
                response.sendRedirect("lecture_carte.jsp");
            } else {
                System.out.println("Erreur lors de l'ajout du soldat en base de données.");
                response.sendRedirect("error.jsp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur de base de données.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}
