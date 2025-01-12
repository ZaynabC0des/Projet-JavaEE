package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Soldat;
import model.SoldatBDD;

@WebServlet("/SoldierServlet")
public class SoldierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("userLogin");
        String gameCode = (String) session.getAttribute("code");
        String soldatIdParam = request.getParameter("soldatId");

        
        if (username == null || gameCode == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Utilisateur non authentifié.\"}");
            return;
        }
        
        if (soldatIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"ID du soldat non fourni.\"}");
            return;
        }

        try {
            int soldatId = Integer.parseInt(soldatIdParam);

            SoldatBDD soldatBDD = new SoldatBDD();
            Soldat soldat = soldatBDD.getSoldatById(soldatId);

            // Vérifie que le soldat appartient bien à l'utilisateur
            if (soldat == null || !soldat.getOwner().equals(username)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\": false, \"message\": \"Soldat non trouvé ou non autorisé.\"}");
                return;
            }

            if (soldat.getPointDeVie() < 50) {
                int newPointsDeVie = Math.min(soldat.getPointDeVie() + 15, 100);
                soldatBDD.updatePointsDeVie(soldatId, newPointsDeVie);
                response.getWriter().write("{\"success\": true, \"message\": \"Le soldat a été soigné avec succès.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Le soldat n'a pas besoin de soin.\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"ID du soldat invalide.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Erreur lors du soin du soldat.\"}");
        }
    }
}