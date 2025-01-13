package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/DestroyForestServlet")
public class DestroyForestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupérer les paramètres x et y
        String xParam = request.getParameter("x");
        String yParam = request.getParameter("y");

        // Vérifier qu'on a bien x et y
        if (xParam == null || yParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètres x et y manquants");
            return;
        }

        // Convertir en int
        int x;
        int y;
        try {
            x = Integer.parseInt(xParam);
            y = Integer.parseInt(yParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètres x et y invalides");
            return;
        }

        // Récupérer la grille depuis la session
        // Supposons que c'est un int[][]
        int[][] grille = (int[][]) request.getSession().getAttribute("grille");
        if (grille == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Aucune grille en session");
            return;
        }



        // Détruire la forêt : par exemple on met 0 (case vide)
        grille[x][y] = 0;

        // Mettre à jour la grille en session
        request.getSession().setAttribute("grille", grille);
        MoveSoldatServlet.updateMap(request.getSession());
        // (Optionnel) Réponse JSON indiquant que tout s'est bien passé
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":true,\"message\":\"Forêt détruite en (" + x + "," + y + ")\"}");

    }

    // Si vous utilisez la méthode POST, redirigez vers doGet() ou implémentez doPost() de manière similaire.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
