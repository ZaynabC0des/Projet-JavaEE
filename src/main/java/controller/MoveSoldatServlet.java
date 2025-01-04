package controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Soldat;
import model.SoldatBDD;

@WebServlet("/MoveSoldatServlet")
public class MoveSoldatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String loggedInUser = (String) session.getAttribute("userLogin"); // Utilisateur connecté
        String soldatIdParam = request.getParameter("soldatId");
        String direction = request.getParameter("direction");

        System.out.println("Requête reçue : soldatId=" + soldatIdParam + ", direction=" + direction);

        if (soldatIdParam == null || direction == null) {
            System.out.println("Paramètres manquants !");
            response.getWriter().write("{\"success\": false, \"message\": \"Paramètres manquants.\"}");
            return;
        }

        int soldatId = Integer.parseInt(soldatIdParam);
        SoldatBDD soldatBDD = new SoldatBDD();
        Soldat soldat = null;

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

        int newX = soldat.getX();
        int newY = soldat.getY();
        System.out.println("Position actuelle du soldat : (" + newX + ", " + newY + ")");

        switch (direction) {
            case "up":
                newX--;
                break;
            case "down":
                newX++;
                break;
            case "left":
                newY--;
                break;
            case "right":
                newY++;
                break;
            default:
                response.getWriter().write("{\"success\": false, \"message\": \"Direction invalide.\"}");
                return;
        }

        System.out.println("Nouvelle position proposée : (" + newX + ", " + newY + ")");

        int[][] grille = (int[][]) session.getAttribute("grille");
        if (grille != null && newX >= 0 && newY >= 0 && newX < grille.length && newY < grille[0].length) {
            if (grille[newX][newY] == 2) { // Vérification si la case est une forêt
                session.setAttribute("proposedPosition", newX + "," + newY);
                session.setAttribute("askDestroyForest", true); // Demande de confirmation pour détruire la forêt
                session.setAttribute("forestPosition", newX + "," + newY); // Sauvegarde de la position de la forêt
                System.out.println("Arrivée sur une case forêt en position (" + newX + ", " + newY + ")");
                response.sendRedirect("lecture_carte.jsp");
            } else if (grille[newX][newY] == 0) {
                boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
                if (success) {
                    grille[soldat.getX()][soldat.getY()] = 0; // Libérer l'ancienne position
                    grille[newX][newY] = soldatId; // Occuper la nouvelle position
                    session.setAttribute("grille", grille);
                    response.getWriter().write("{\"success\": true}");
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Erreur de mise à jour en base.\"}");
                }
            } else {
                System.out.println("Case occupée à (" + newX + ", " + newY + ")");
                response.getWriter().write("{\"success\": false, \"message\": \"Case occupée.\"}");
            }
        } else {
            System.out.println("Position hors limite ou grille non définie.");
            response.getWriter().write("{\"success\": false, \"message\": \"Position hors limite.\"}");
        }
    }catch (SQLException e) {
        e.printStackTrace();
        response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne : " + e.getMessage() + "\"}");
    }
}
    
}
