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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/UpdatePositionServlet")
public class UpdatePositionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Récupération de la session utilisateur
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifié.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        // Récupération des attributs de la session
        String csvFilePath = (String) session.getAttribute("userFilePath");
        int[][] grille = (int[][]) session.getAttribute("grille");
        if (csvFilePath == null || grille == null) {
            System.out.println("Erreur : Données utilisateur ou grille manquantes.");
            response.sendRedirect("lecture_carte.jsp");
            return;
        }

        // Paramètres de la requête
        String[] positionParts = request.getParameter("position").split(",");
        int x = Integer.parseInt(positionParts[0]);
        int y = Integer.parseInt(positionParts[1]);
        String action = request.getParameter("action");

        try {
            if ("destroyForest".equals(action)) {
                handleDestroyForest(session, user, csvFilePath, grille, x, y);
            } else if ("moveOnly".equals(action)) {
                handleMoveOnly(session, x, y);
            } else {
                System.out.println("Action inconnue : " + action);
            } 
            
            // Vérification des points de production
            checkProductionPoints(session, user);
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur lors de la mise à jour des points de production.");
        }

        // Redirection vers la carte mise à jour
        response.sendRedirect("lecture_carte.jsp");
    }

    private void handleDestroyForest(HttpSession session, User user, String csvFilePath, int[][] grille, int x, int y) throws IOException, SQLException {
        // Vérifie si la case est bien une forêt
        if (grille[x][y] == 2) {
            grille[x][y] = 0;     // Mise à jour de la grille
            session.setAttribute("grille", grille);

            // Mise à jour du fichier CSV
            List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
            String[] row = lines.get(x).split(",");
            row[y] = "0";
            lines.set(x, String.join(",", row));

            try (FileWriter writer = new FileWriter(new File(csvFilePath), false)) {
                for (String line : lines) {
                    writer.write(line + "\n");
                }
            }

            // Mise à jour des points de production
            UserBDD userBDD = new UserBDD();
            if (userBDD.updateProductionPoints(user.getLogin(), 2)) {  // Ajoute 2 points
                int updatedPoints = userBDD.getProductionPoints(user.getLogin());
                session.setAttribute("productionPoints", updatedPoints);
                System.out.println("2 points de production ajoutés pour destruction d'un arbre.");
            } else {
                System.out.println("Erreur lors de l'ajout des points de production.");
            }

            System.out.println("Forêt détruite en position (" + x + ", " + y + ").");
        } else {
            System.out.println("Erreur : La case (" + x + ", " + y + ") n'est pas une forêt.");
        }

        // Mise à jour de la position du joueur
        session.setAttribute("playerPosition", x + "," + y);
    }

 
    private void handleMoveOnly(HttpSession session, int x, int y) {
        // Mise à jour uniquement de la position
        session.setAttribute("playerPosition", x + "," + y);
        System.out.println("Déplacement sans destruction en position (" + x + ", " + y + ").");
    }
    
    private void checkProductionPoints(HttpSession session, User user) throws SQLException {
        UserBDD userBDD = new UserBDD();
        boolean canRecruit = userBDD.checkProductionPoints(user.getLogin());
        session.setAttribute("canRecruit", canRecruit); // Ajouter cette information dans la session
    }
}
