package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import model.UserBDD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public LoginServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        User u1 = new User(login, password);
        UserBDD utable = new UserBDD();

        try {
            User foundUser = utable.findUser(u1);
            if (foundUser != null) {
                session.setAttribute("user", foundUser);
                System.out.println("Utilisateur trouv� : " + foundUser.getLogin());

                // Chemin vers le fichier CSV de l'utilisateur
                String baseDir = "C:\\Users\\CYTech Student\\eclipse-workspace\\projet\\src\\main\\webapp\\maps";
                String userDir = Paths.get(baseDir, login).toString();
                String userFilePath = Paths.get(userDir, login + ".csv").toString();

                if (new File(userFilePath).exists()) {
                    session.setAttribute("userFilePath", userFilePath);

                    // Initialiser la grille
                    try {
                        int[][] grille = initializeGrid(userFilePath);
                        session.setAttribute("grille", grille);
                       // System.out.println("Grille initialis�e avec succ�s.");
                        response.sendRedirect("lecture_carte.jsp");
                        
                    } catch (IOException e) {
                    	System.out.println("Erreur lors de l'initialisation de la grille : " + e.getMessage());
                        request.setAttribute("error", "Erreur lors de l'initialisation de la carte : " + e.getMessage());
                        request.getRequestDispatcher("connexion.jsp").forward(request, response);
                    }
                } else {
                    request.setAttribute("error", "Fichier CSV non trouv�.");
                    request.getRequestDispatcher("connexion.jsp").forward(request, response);
                }
            } else {
                request.setAttribute("error", "Identifiants incorrects ou utilisateur non trouv�.");
                request.getRequestDispatcher("connexion.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Erreur de connexion � la base de donn�es : " + e.getMessage());
            if (e.getErrorCode() == 0) {
                System.out.println("Pas connect� � la BDD");
            }
        }
    }

    /**
     * M�thode pour lire le fichier CSV et initialiser la grille.
     * 
     * @param filePath Chemin vers le fichier CSV
     * @return Grille sous forme de tableau 2D d'entiers
     * @throws IOException En cas d'erreur de lecture du fichier
     */
    private int[][] initializeGrid(String filePath) throws IOException {
        List<int[]> tempGrid = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int[] row = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Integer.parseInt(values[i].trim());
                }
                tempGrid.add(row);
            }
        }

        // Convertir la liste temporaire en tableau 2D
        return tempGrid.toArray(new int[tempGrid.size()][]);
    }
}
