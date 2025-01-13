package controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.*;
import jakarta.websocket.Session;



@WebServlet("/MoveSoldatServlet")
public class MoveSoldatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static void updateMap(HttpSession session) throws IOException {
        String filePath = "H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\csv\\" + (String) session.getAttribute("code") + ".csv";
        int[][] grille = (int[][]) session.getAttribute("grille");
        try (FileWriter writer = new FileWriter(filePath)) {
            for (int[] row : grille) {
                for (int cell : row) {
                    writer.write(cell + ",");
                }
                writer.write("\n");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String loggedInUser = (String) session.getAttribute("userLogin"); // Utilisateur connecté
        String soldatIdParam = request.getParameter("soldatId");
        String direction = request.getParameter("direction");
        UserBDD userBDD = new UserBDD();
        if (soldatIdParam == null || direction == null) {
            System.out.println("Paramètres manquants !");
            response.getWriter().write("{\"success\": false, \"message\": \"Paramètres manquants.\"}");
            return;
        }

    int soldatId = Integer.parseInt(soldatIdParam);
    SoldatBDD soldatBDD = new SoldatBDD();
    Soldat soldat;

    try {
        soldat = soldatBDD.getSoldatById(soldatId);

        if (soldat == null) {
            System.out.println("Soldat non trouvé avec ID=" + soldatId);
            response.getWriter().write("{\"success\": false, \"message\": \"Soldat non trouvé.\"}");
            return;
        }

        // Vérification : Est-ce que l'utilisateur connecté est le propriétaire du soldat ?
        if (!soldat.getOwner().equals(loggedInUser)) {
            response.getWriter().write("{\"success\": false, \"message\": \"Vous ne pouvez pas déplacer ce soldat.\"}");
            return;
        }

        // Transmettre les informations à la JSP via la session
        session.setAttribute("combatOwner", soldat.getOwner());

        int newX = soldat.getX();
        int newY = soldat.getY();
        System.out.println("Position actuelle du soldat : (" + newX + ", " + newY + ")");

        switch (direction) {
            case "up": newX--; break;
            case "down": newX++; break;
            case "left": newY--; break;
            case "right": newY++; break;
            default: response.getWriter().write("{\"success\": false, \"message\": \"Direction invalide.\"}"); return;
        }

        int[][] grille = (int[][]) session.getAttribute("grille");
        if (grille == null || newX < 0 || newY < 0 || newX >= grille.length || newY >= grille[0].length) {
            response.getWriter().write("{\"success\": false, \"message\": \"Position hors limite.\"}");
            return;
        }

        switch (grille[newX][newY]) {
            case 3:
                session.removeAttribute("combat");
                session.setAttribute("showPopup", true);
                session.setAttribute("errorMessage", "Mouvement bloqué : Montagne non franchissable.");
                response.sendRedirect("lecture_carte.jsp");
                return;
            case 2:
                // Déplacement du soldat sur la case forêt
                boolean success = soldatBDD.updatePosition(soldatId, newX, newY);

                if (success) {


                    // Signaler qu'un popup doit être affiché								//code ajouté
                    session.setAttribute("showForestPopup", true);
                    session.setAttribute("forestPosition", newX + "," + newY);
                    session.setAttribute("soldatId", soldatId);


                    grille[newX][newY] = 0; // Remplacer la forêt par une case vide

                    session.setAttribute("grille", grille);
                    updateMap(session);
                    GameWebSocket.broadcastDestroyForest(loggedInUser,(String)session.getAttribute("code"),newX,newY,soldatId);

                    userBDD.updateProductionPoints(loggedInUser, 10);
                    session.setAttribute("productionPoints", userBDD.getProductionPoints(loggedInUser));



                    System.out.println("Forêt détruite en position (" + newX + ", " + newY + ").");

                    }

                System.out.println("Soldat dans la forêt, popup activé.");
                response.getWriter().write("{\"success\": true, \"message\": \"Dans une forêt.\"}");

                return;
        case 1:
            String owner = null;
            try {

                if (owner == null) {
                    VilleBDD villeBDD = new VilleBDD();
                    int defensePoints = villeBDD.getCityDefensePoints(newX, newY);

                    // R�initialiser le combat uniquement si n�cessaire
                    Combat combat = (Combat) session.getAttribute("combat");
                    if (combat == null || combat.getPointsDefenseCible() != defensePoints) {
                        System.out.println("R�initialisation du combat avec " + defensePoints + " points de d�fense.");
                        combat = new Combat(defensePoints, "ville");
                        session.setAttribute("combat", combat);
                        //session.setAttribute("combatDefensePoints", combat.getPointsDefenseCible());
                    } else {
                        System.out.println("Combat deja initialis avec les points de d�fense actuels.");
                    }

                    if (request.getParameter("attaquer") == null) {
                        Random random = (Random) session.getAttribute("random");
                        if (random == null) {
                            random = new Random();
                            session.setAttribute("random", random);
                        }
                        int lancerDe = random.nextInt(6) + 1;
                        combat.Attaque(lancerDe);

                        if (combat.getPointsDefenseCible() != defensePoints) {
                            villeBDD.updateDefensePoints(newX, newY, combat.getPointsDefenseCible());
                        }
                        session.setAttribute("combatDefensePoints", combat.getPointsDefenseCible());
                        System.out.println("R�sultat de l'attaque : " + combat.getPointsDefenseCible());
                    }



                    // Mettre à jour les points de défense
                    int updatedDefensePoints = combat.getPointsDefenseCible(); // Variable déclarée ici
                    villeBDD.updateDefensePoints(newX, newY, updatedDefensePoints);
                    session.setAttribute("combatDefensePoints", updatedDefensePoints);
                    //int defensePoints = villeBDD.getCityDefensePoints(newX, newY);

                    if (updatedDefensePoints == 0 ) {
                    	 String soldatOwner = soldatBDD.getLoginBySoldatId(soldatId); // Récupère le propriétaire du soldat

                         // Ville capturée
                         villeBDD.updateCityOwner(newX, newY, soldatOwner); // Mise à jour du propriétaire
                         session.setAttribute("combatMessage", "Ville capturée par " + loggedInUser);
                         System.out.println("Ville capturée par " + loggedInUser + " en position (" + newX + ", " + newY + ").");

                         session.setAttribute("combatMessage", "Ville capturée par " + loggedInUser);
                         session.setAttribute("nombreVilles", userBDD.compterVillesPossedeesParUtilisateur(loggedInUser));
                         if (soldatOwner == loggedInUser) {
                        	 userBDD.updateScore(loggedInUser, 25);
                             int new_score = userBDD.getUserScore(loggedInUser);
                             session.setAttribute("score", new_score);
                         }

                    } else {
                        session.setAttribute("combatMessage", "Combat en cours...");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Erreur lors de la r�cup�ration ou de la mise � jour des points de d�fense : " + e.getMessage());
            }
            break;

    case 0:
        session.removeAttribute("combat");
        System.out.println("Position mise à jour : (" + newX + ", " + newY + ")");
        break;
}

	boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
	if (success) {
		// Ajouter 10 points de production pour le propriétaire du soldat
	    userBDD.updateProductionPoints(loggedInUser, 5);

	    // Mettre à jour les points de production dans la session
	    session.setAttribute("productionPoints", userBDD.getProductionPoints(loggedInUser));
	    System.out.println("5 points de production ajoutés pour le joueur " + loggedInUser);
	    response.getWriter().write("{\"success\": true}");
	} else {
	    response.getWriter().write("{\"success\": false, \"message\": \"Erreur de mise à jour en base.\"}");
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	    response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne : " + e.getMessage() + "\"}");
	    }
	}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response); // Redirige les requ�tes POST vers doGet
    }

}