package controller;

import java.io.IOException;

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
        String soldatIdParam = request.getParameter("soldatId");
        String direction = request.getParameter("direction");

        System.out.println("Requ�te re�ue : soldatId=" + soldatIdParam + ", direction=" + direction);

        if (soldatIdParam == null || direction == null) {
            System.out.println("Param�tres manquants !");
            response.getWriter().write("{\"success\": false, \"message\": \"Param�tres manquants.\"}");
            return;
        }

        int soldatId = Integer.parseInt(soldatIdParam);
        SoldatBDD soldatBDD = new SoldatBDD();
        Soldat soldat = soldatBDD.getSoldatById(soldatId);

        if (soldat == null) {
            System.out.println("Soldat non trouv� avec ID=" + soldatId);
            response.getWriter().write("{\"success\": false, \"message\": \"Soldat non trouv�.\"}");
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
                System.out.println("Direction invalide : " + direction);
                response.getWriter().write("{\"success\": false, \"message\": \"Direction invalide.\"}");
                return;
        }

        System.out.println("Nouvelle position propos�e : (" + newX + ", " + newY + ")");

     // V�rifie que la nouvelle position est libre
        int[][] grille = (int[][]) session.getAttribute("grille");
        if (grille != null && newX >= 0 && newY >= 0 && newX < grille.length && newY < grille[0].length) {
            if (grille[newX][newY] == 0) {
            	//mettre a jours la bdd 
                boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
                if (success) {
                	//mise a jours de la grille 
                    System.out.println("Position mise � jour avec succ�s dans la base !");
                    grille[soldat.getX()][soldat.getY()] = 0; // Lib�rer l'ancienne position
                    grille[newX][newY] = soldatId; // Occuper la nouvelle position
                    session.setAttribute("grille", grille);
                    response.getWriter().write("{\"success\": true}");
                } else {
                    System.out.println("Erreur lors de la mise � jour en base.");
                    response.getWriter().write("{\"success\": false, \"message\": \"Erreur de mise � jour en base.\"}");
                }
            } else {
                System.out.println("Case occup�e � (" + newX + ", " + newY + ")");
                response.getWriter().write("{\"success\": false, \"message\": \"Case occup�e.\"}");
            }
        } else {
            System.out.println("Position hors limite ou grille non d�finie.");
            response.getWriter().write("{\"success\": false, \"message\": \"Position hors limite.\"}");
        }
    }
    
}
