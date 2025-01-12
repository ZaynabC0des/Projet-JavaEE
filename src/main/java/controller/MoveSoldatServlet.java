package controller;

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
        String loggedInUser = (String) session.getAttribute("userLogin"); // Utilisateur connecté
        String soldatIdParam = request.getParameter("soldatId");
        String direction = request.getParameter("direction");
        String action = request.getParameter("action");

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
                case 1:
                    handleCityEncounter(session, newX, newY, request, response);
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

    private void handleCityEncounter(HttpSession session, int x, int y, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        VilleBDD villeBDD = new VilleBDD();
        String userLogin = (String) session.getAttribute("userLogin");
        String owner = villeBDD.getCityOwner(x, y);  // RÃ©cupÃ©ration du propriÃ©taire actuel de la ville

        if (owner == null || !owner.equals(userLogin)) {
            // RÃ©cupÃ©ration dynamique des points de dÃ©fense de la ville Ã  partir de la base de donnÃ©es
            int defensePoints = villeBDD.getCityDefensePoints(x, y);
            Combat combat = new Combat(defensePoints, "ville");
            session.setAttribute("combat", combat);

            // VÃ©rification si une action d'attaque est initiÃ©e
            if ("attaquer".equals(request.getParameter("action"))) {
                Random random = new Random();
                int lancerDe = random.nextInt(6) + 1;
                String combatResult = combat.Attaque(lancerDe);
                session.setAttribute("combatResult", combatResult);
                session.setAttribute("lancerDe", lancerDe);
                session.setAttribute("combatActive", true);
                villeBDD.updateDefensePoints(x, y, combat.getPointsDefenseCible());

                if (!combat.estCibleEnVie()) {
                    villeBDD.updateCityOwner(x, y, userLogin);  // La ville est capturÃ©e si le combat est gagnÃ©
                    session.setAttribute("combatActive", false);
                }

                response.sendRedirect("lecture_carte.jsp");  // Redirection pour afficher les rÃ©sultats du combat
                session.setAttribute("isSoldierOnCity", true);
                session.setAttribute("currentSoldierId", request.getParameter("soldatId"));
            }
        } else {
            System.out.println("La ville appartient dÃ©jÃ  au joueur ou est libre, aucun combat nÃ©cessaire.");

        }
    }




}
