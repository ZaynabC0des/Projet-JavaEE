package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Combat;
import model.Soldat;
import model.SoldatBDD;
import model.UserBDD;
import model.VilleBDD;

@WebServlet("/MoveSoldatServlet")
public class MoveSoldatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String loggedInUser = (String) session.getAttribute("userLogin");
        String soldatIdParam = request.getParameter("soldatId");
        String direction = request.getParameter("direction");
        String action = request.getParameter("attaquer");

        if (soldatIdParam == null || direction == null) {
            response.getWriter().write("{\"success\": false, \"message\": \"ParamÃ¨tres manquants.\"}");
            return;
        }

        int soldatId = Integer.parseInt(soldatIdParam);
        SoldatBDD soldatBDD = new SoldatBDD();
        Soldat soldat;

        try {
            soldat = soldatBDD.getSoldatById(soldatId);
            if (soldat == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Soldat non trouvÃ©.\"}");
                return;
            }

            if (!soldat.getOwner().equals(loggedInUser)) {
                response.getWriter().write("{\"success\": false, \"message\": \"Vous ne pouvez pas dÃ©placer ce soldat.\"}");
                return;
            }

            int newX = soldat.getX();
            int newY = soldat.getY();

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
                    session.setAttribute("errorMessage", "Mouvement bloquÃ© : Montagne non franchissable.");
                    response.sendRedirect("lecture_carte.jsp");
                    return;
                case 2:
                    session.removeAttribute("combat");
                    session.setAttribute("proposedPosition", newX + "," + newY);
                    session.setAttribute("askDestroyForest", true);
                    session.setAttribute("forestPosition", newX + "," + newY);
                    response.sendRedirect("lecture_carte.jsp");
                    return;
        case 1:
            String owner = null;
            try {
               
                if (owner == null) {
                    VilleBDD villeBDD = new VilleBDD();
                    int defensePoints = villeBDD.getCityDefensePoints(newX, newY);

                    // Réinitialiser le combat uniquement si nécessaire
                    Combat combat = (Combat) session.getAttribute("combat");
                    if (combat == null || combat.getPointsDefenseCible() != defensePoints) {
                        System.out.println("Réinitialisation du combat avec " + defensePoints + " points de défense.");
                        combat = new Combat(defensePoints, "ville");
                        session.setAttribute("combat", combat);
                        //session.setAttribute("combatDefensePoints", combat.getPointsDefenseCible());
                    } else {
                        System.out.println("Combat déjà initialisé avec les points de défense actuels.");
                    }

                    if (request.getParameter("attaquer") == null) {
                    	System.out.println("aezrty");
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
                        System.out.println("Résultat de l'attaque : " + combat.getPointsDefenseCible());
                    }
                } else {
                    System.out.println("La ville appartient au joueur actuel.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Erreur lors de la récupération ou de la mise à jour des points de défense : " + e.getMessage());
            }
            break;

        
       

    case 0:
        session.removeAttribute("combat");
        System.out.println("Position mise Ã  jour : (" + newX + ", " + newY + ")");
        break;
}

boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
if (success) {
    grille[soldat.getX()][soldat.getY()] = 0;
    grille[newX][newY] = soldatId;
    session.setAttribute("grille", grille);
    response.getWriter().write("{\"success\": true}");
} else {
    response.getWriter().write("{\"success\": false, \"message\": \"Erreur de mise Ã  jour en base.\"}");
    }
} catch (SQLException e) {
    e.printStackTrace();
    response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne : " + e.getMessage() + "\"}");
    }
}

   

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response); // Redirige les requêtes POST vers doGet
    }

}
