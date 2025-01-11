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
import controller.GameWebSocket;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static controller.GameWebSocket.currentPlayerIndex;

@WebServlet("/RecruitSoldierServlet")
public class RecruitSoldierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userLogin = (String) session.getAttribute("userLogin");


        if(!GameWebSocket.playersOrder.get(currentPlayerIndex).username.equals(userLogin)){
            System.out.println("Erreur : Ce n'est pas le tour de ce joueur."+GameWebSocket.playersOrder.get(currentPlayerIndex).username);
            response.sendRedirect("lecture_carte.jsp");
            return;
        }

        if (userLogin.isEmpty()) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié ou le login est invalide.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        UserBDD userBDD = new UserBDD();
        SoldatBDD soldatBDD = new SoldatBDD();
        
        try {
            int currentPoints = userBDD.getProductionPoints(userLogin);
            System.out.println("Points de production pour le joueur " + userLogin + ": " + currentPoints);
            if (currentPoints >= 15 ) {
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
                Collections.shuffle(emptyPositions, new Random(12845));

                if (emptyPositions.isEmpty()) {
                    System.out.println("Erreur : Aucune case vide disponible pour positionner un soldat.");
                    response.sendRedirect("lecture_carte.jsp");
                    return;
                }
                Integer x=null;
                Integer y=null;
                for (int[] emptyPosition : emptyPositions) {
                    if (soldatBDD.existeSoldatPosition(emptyPosition[0], emptyPosition[1],(String)session.getAttribute("code"))) {
                        System.out.println("Erreur : Une autre entité occupe déjà la position (" + emptyPosition[0] + ", " + emptyPosition[1] + ").");

                    } else {
                        x = emptyPosition[0];
                        y = emptyPosition[1];
                        break;
                    }
                }
                if(x==null || y==null){
                    System.out.println("Erreur : Aucune case vide disponible pour positionner un soldat.");
                    response.sendRedirect("lecture_carte.jsp");
                    return;
                }


                // Ajouter le soldat à la base de données avec ses coordonnées
                int soldatId = soldatBDD.ajouterSoldatEtRecupererId(userLogin, 100, x, y,(String)session.getAttribute("code"));

                if (soldatId != -1) {
                    String json = String.format(
                            "{\"type\":\"move\",\"username\":\"%s\",\"soldatId\":%d,\"code\":\"%s\"}",
                            userLogin, soldatId,session.getAttribute("code")
                    );
                    GameWebSocket.broadcastMessage(json);
                    if(GameWebSocket.players.size()>1){

                        GameWebSocket.nextTurn();

                    }
                    // Mettre à jour la grille
                    grille[x][y] = 4; // 4 représente un soldat
                    session.setAttribute("grille", grille);

                    // Déduire les points de production du joueur
                    userBDD.updateProductionPoints(userLogin, -15);
                    int nombreSoldats = userBDD.compterSoldatsPossedesParUtilisateur(userLogin, (String)session.getAttribute("code"));
                    session.setAttribute("nombreSoldats", nombreSoldats);

                    System.out.println("Soldat ajouté avec succès à la position (" + x + ", " + y + "), ID: " + soldatId);
                    response.sendRedirect("lecture_carte.jsp");
                } else {
                    System.out.println("Erreur lors de l'ajout du soldat en base de données.");
                    response.sendRedirect("error.jsp");
                }
            } else {

                System.out.println("Pas assez de points de production pour recruter un soldat.");
                request.setAttribute("error", "Pas assez de points de production pour recruter un soldat.");
                request.getRequestDispatcher("lecture_carte.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur de base de données.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}