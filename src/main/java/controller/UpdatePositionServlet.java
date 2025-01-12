package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import model.UserBDD;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/UpdatePositionServlet")
public class UpdatePositionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        System.out.println("DEBUG : Paramètres reçus = " + request.getQueryString());     //rajouté ça

        if (user == null) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        String csvFilePath = (String) session.getAttribute("userFilePath");
        int[][] grille = (int[][]) session.getAttribute("grille");
        if (csvFilePath == null || grille == null) {
            System.out.println("Erreur : Données utilisateur ou grille manquantes.");
            response.sendRedirect("lecture_carte.jsp");
            return;
        }

        String[] positionParts = request.getParameter("position").split(",");
        int x = Integer.parseInt(positionParts[0]);
        int y = Integer.parseInt(positionParts[1]);
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "destroyForest":														//j'ai mis ça
                    handleDestroyForest(session, user, csvFilePath, grille, x, y);
                    break;
                case "moveOnly":
                    handleMoveOnly(session, x, y);
                    break;
                case "forage":
                    handleForage(session, user, x, y);
                    break;
                default:
                    System.out.println("Action inconnue : " + action);
                    response.getWriter().write("{\"success\": false, \"message\": \"Action inconnue.\"}");
                    return;
            }

            checkProductionPoints(session, user);

            // Rappelez lecture_carte.jsp pour recharger la grille
            response.sendRedirect("lecture_carte.jsp");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne.\"}");
        }
    }
    
    private void handleForage(HttpSession session, User user, int x, int y) throws SQLException, ClassNotFoundException {
        int[][] grille = (int[][]) session.getAttribute("grille");

        if (grille[x][y] == 2) {
            UserBDD userBDD = new UserBDD();
            int pointsGained = 1 + (int) (Math.random() * 5);

            if (userBDD.updateProductionPoints(user.getLogin(), pointsGained)) {
                int updatedPoints = userBDD.getProductionPoints(user.getLogin());
                session.setAttribute("productionPoints", updatedPoints);
                System.out.println("Ressources fourragées : " + pointsGained + " points gagnés.");
            } else {
                System.out.println("Erreur lors de l'ajout des points de production.");
            }
        } else {
            System.out.println("Erreur : La case (" + x + ", " + y + ") n'est pas une forêt.");
        }
    }
    																							//code mis à jour
    private void handleDestroyForest(HttpSession session, User user, String csvFilePath, int[][] grille, int x, int y) 
            throws IOException, SQLException, ClassNotFoundException {
        grille = reloadGrilleFromCSV(csvFilePath);
        session.setAttribute("grille", grille);
        System.out.println("DEBUG : Grille rechargée depuis le fichier CSV.");

        if (grille[x][y] == 2) {
            grille[x][y] = 0;
            session.setAttribute("grille", grille);

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/base_projet_jee", "root", "");
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM foret WHERE x_position = ? AND y_position = ?")) {
                pstmt.setInt(1, x);
                pstmt.setInt(2, y);
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Forêt supprimée en base de données pour la position (" + x + ", " + y + ").");
                } else {
                    System.out.println("Aucune forêt trouvée en base de données à la position (" + x + ", " + y + ").");
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la suppression de la forêt en base de données : " + e.getMessage());
                throw e;
            }

            List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
            String[] row = lines.get(x).split(",");
            row[y] = "0";
            lines.set(x, String.join(",", row));

            try (FileWriter writer = new FileWriter(new File(csvFilePath), false)) {
                for (String line : lines) {
                    writer.write(line + "\n");
                }
            }

            UserBDD userBDD = new UserBDD();
            if (userBDD.updateProductionPoints(user.getLogin(), 2)) {
                int updatedPoints = userBDD.getProductionPoints(user.getLogin());
                session.setAttribute("productionPoints", updatedPoints);
                System.out.println("2 points de production ajoutés pour destruction d'une forêt.");
            } else {
                System.out.println("Erreur lors de l'ajout des points de production.");
            }

            System.out.println("Forêt détruite en position (" + x + ", " + y + ").");
        } else {
            System.out.println("Erreur : La case (" + x + ", " + y + ") n'est pas une forêt.");
        }

        //session.setAttribute("playerPosition", x + "," + y);
    }

    private void handleMoveOnly(HttpSession session, int x, int y) {
        session.setAttribute("playerPosition", x + "," + y);
        System.out.println("Déplacement sans destruction en position (" + x + ", " + y + ").");
    }

    private void checkProductionPoints(HttpSession session, User user) throws SQLException, ClassNotFoundException {
        UserBDD userBDD = new UserBDD();
        boolean canRecruit = userBDD.checkProductionPoints(user.getLogin());
        session.setAttribute("canRecruit", canRecruit);
    }
    																			                     //code mis à jour 
    private int[][] reloadGrilleFromCSV(String csvFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        int[][] grille = new int[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            grille[i] = new int[parts.length];
            for (int j = 0; j < parts.length; j++) {
                grille[i][j] = Integer.parseInt(parts[j].trim());
            }
        }
        return grille;
    }
}
