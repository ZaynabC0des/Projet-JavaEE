package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
                if (grille[newX][newY] == 2) { // Case forêt
                    // Déplacement du soldat sur la case forêt
                    boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
                    if (success) {
                        grille[soldat.getX()][soldat.getY()] = 0; // Libérer l'ancienne position
                        grille[newX][newY] = soldatId; // Occuper la nouvelle position
                        session.setAttribute("grille", grille);

                        // Signaler qu'un popup doit être affiché								//code ajouté 
                        session.setAttribute("showForestPopup", true);
                        session.setAttribute("forestPosition", newX + "," + newY);
                        session.setAttribute("soldatId", soldatId);
                        																			//code à partir de la 
                        // Vérifier si l'utilisateur a confirmé la destruction
                        String destroyForest = request.getParameter("destroyForest");
                        if ("true".equals(destroyForest)) {
                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/base_projet_jee", "root", "");
                                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM foret WHERE x_position = ? AND y_position = ?")) {
                                pstmt.setInt(1, newX);
                                pstmt.setInt(2, newY);
                                int rowsDeleted = pstmt.executeUpdate();
                                if (rowsDeleted > 0) {
                                    grille[newX][newY] = 0; // Remplacer la forêt par une case vide
                                    session.setAttribute("grille", grille);
                                    System.out.println("Forêt détruite en position (" + newX + ", " + newY + ").");
                                } else {
                                    System.out.println("Erreur : aucune forêt trouvée en base à la position (" + newX + ", " + newY + ").");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                response.getWriter().write("{\"success\": false, \"message\": \"Erreur lors de la destruction de la forêt.\"}");
                                return;
                            }
                        }

                        System.out.println("Soldat dans la forêt, popup activé.");
                        response.getWriter().write("{\"success\": true, \"message\": \"Dans une forêt.\"}");
                    } else {
                        response.getWriter().write("{\"success\": false, \"message\": \"Erreur de mise à jour en base.\"}");
                    }
                } else if (grille[newX][newY] == 0) { // Case vide
                    boolean success = soldatBDD.updatePosition(soldatId, newX, newY);
                    if (success) {
                        grille[soldat.getX()][soldat.getY()] = 0;
                        grille[newX][newY] = soldatId;
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
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne : " + e.getMessage() + "\"}");
        }
    }
}
