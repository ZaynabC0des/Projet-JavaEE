package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.SoldatBDD;
import model.UserBDD;
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
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	
    	HttpSession session = request.getSession();
        String userLogin = (String) session.getAttribute("userLogin");

        if (GameWebSocket.playersOrder.isEmpty() || currentPlayerIndex >= GameWebSocket.playersOrder.size()) {
            System.out.println("Erreur : Aucun joueur dans la liste ou index invalide.");
            response.getWriter().write("{\"success\": false, \"message\": \"Aucun joueur dans la liste ou index invalide.\"}");
            return;
        }

        if(!GameWebSocket.playersOrder.get(currentPlayerIndex).equals(userLogin)){
            System.out.println("Erreur : Ce n'est pas le tour de ce joueur."+GameWebSocket.playersOrder.get(currentPlayerIndex));
            response.getWriter().write("{\"success\": false, \"message\": \"Ce n'est pas votre tour.\"}");
            return;
        }

        if (userLogin == null || userLogin.isEmpty()) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié ou le login est invalide.");
            response.getWriter().write("{\"success\": false, \"message\": \"Utilisateur non authentifié.\"}");
            return;
        }

        UserBDD userBDD = new UserBDD();
        SoldatBDD soldatBDD = new SoldatBDD();
        
        try {
            int currentPoints = userBDD.getProductionPoints(userLogin);
            System.out.println("Points de production pour le joueur " + userLogin + ": " + currentPoints);
            
            if (currentPoints < 15) {
                System.out.println("Pas assez de points de production pour recruter un soldat.");
                response.getWriter().write("{\"success\": false, \"message\": \"Pas assez de points pour recruter un soldat.\"}");
                return;
            }
            // Déduire les points de manière sûre
//            userBDD.updateProductionPoints(userLogin, -15);

                int[][] grille = (int[][]) session.getAttribute("grille");

                if (grille == null) {
                    System.out.println("Erreur : Grille non initialisée.");
                    response.getWriter().write("{\"success\": false, \"message\": \"Grille non initialisée.\"}");
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
                    response.sendRedirect("../views/lecture_carte.jsp");
                    return;
                }
                Integer x=null;
                Integer y=null;
                for (int[] emptyPosition : emptyPositions) {
                    if (soldatBDD.existeSoldatPosition(emptyPosition[0], emptyPosition[1],(String)session.getAttribute("code"))) {
                    	 response.getWriter().write("{\"success\": false, \"message\": \"Aucune case vide disponible pour positionner un soldat.\"}");
                    	 
                    } else {
                        x = emptyPosition[0];
                        y = emptyPosition[1];
                        break;
                    }
                }
                if(x==null || y==null){
                    System.out.println("Erreur : Aucune case vide disponible pour positionner un soldat.");
                    response.getWriter().write("{\"success\": false, \"message\": \"Aucune position disponible pour positionner un soldat.\"}");
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



                    // Déduire les points de production du joueur
                    userBDD.updateProductionPoints(userLogin, -15);
                    int newpoints= userBDD.getProductionPoints(userLogin);
                    session.setAttribute("productionPoints",newpoints);
                    int nombreSoldats = userBDD.compterSoldatsPossedesParUtilisateur(userLogin, (String)session.getAttribute("code"));
                    session.setAttribute("nombreSoldats", nombreSoldats);
                    session.setAttribute("score",(int)session.getAttribute("score")+10);
                    userBDD.updateScore(userLogin,(int)session.getAttribute("score"));
                    System.out.println("Soldat ajouté avec succès à la position (" + x + ", " + y + "), ID: " + soldatId);
                    response.getWriter().write("{\"success\": true, \"message\": \"Nouveau soldat ajouté avec succès !\"}");
                } else {
                    System.out.println("Erreur lors de l'ajout du soldat en base de données.");
                    response.getWriter().write("{\"success\": false, \"message\": \"Erreur lors de l'ajout du soldat.\"}");
                }

        } catch (SQLException e) {
        	 e.printStackTrace();
             response.getWriter().write("{\"success\": false, \"message\": \"Erreur de base de données.\"}");
            
        }
    }
}