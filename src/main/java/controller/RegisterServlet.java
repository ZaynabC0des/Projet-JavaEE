package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.UserBDD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.util.List;


@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RegisterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String repeatPassword = request.getParameter("psw-repeat");

        // Check if the passwords match
        if (!password.equals(repeatPassword)) {
            request.setAttribute("error", "Les mots de passe ne correspondent pas.");
            request.getRequestDispatcher("connexion.jsp").forward(request, response);
            return;
        }

        // Check if the passwords match
        if (!password.equals(repeatPassword)) {
            request.setAttribute("error", "Les mots de passe ne correspondent pas.");
            request.getRequestDispatcher("connexion.jsp").forward(request, response);
            return;
        }

        // Hash the password
        String hashedPassword = hashPassword(password);

        User newUser = new User(login, hashedPassword);
        UserBDD utable = new UserBDD();

        try {
            // Check if the user already exists
            if (utable.checkUserExists(login)) {
                request.setAttribute("error", "Le login est deja utilis�. Veuillez choisir un autre login.");
                request.getRequestDispatcher("connexion.jsp").forward(request, response);
                return;
            }

            // Assign an image to the new user
            String soldierImage = assignSoldierImage(login); // G�n�re l'image pour l'utilisateur
            newUser.setSoldierImage(soldierImage); // Associe l'image � l'utilisateur



            // Add the new user
            if (utable.addUser(newUser)) {
                // Create directory for the user
                String baseDir ="H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\maps\\";
                String userDir = Paths.get(baseDir, login).toString();
                new File(userDir).mkdirs();

                // Copy default CSV file to user's directory
                Path sourcePath = Paths.get(baseDir, "..\\csv\\default.csv");
                Path targetPath = Paths.get(userDir, login + ".csv");
                Files.copy(sourcePath, targetPath);

                // Store the path to the user's CSV file in session
                request.getSession().setAttribute("userFilePath", targetPath.toString());

                // Redirect to another page after successful registration
                response.sendRedirect("connexion.jsp");
            } else {
                request.setAttribute("error", "L'inscription a échoué. Erreur lors de l'ajout de l'utilisateur.");
                request.getRequestDispatcher("connexion.jsp").forward(request, response);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur de système: " + e.getMessage());
            request.getRequestDispatcher("connexion.jsp").forward(request, response);
        }
    }

 // Method to hash a password using SHA-256
    static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convert bytes to hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    //test
    private String assignSoldierImage(String username) {
        String[] images = {
                "images/soldats/soldat_bleu.jpg",
                "images/soldats/soldat_vert.jpg",
                "images/soldats/soldat_rouge.jpg",
                "images/soldats/soldat_violet.jpg",
                "images/soldats/soldat_rose.jpg",
                "images/soldats/soldat_jaune.jpg"

        };

        try {
            // Obtenir les images déjà utilisées
            UserBDD userBDD = new UserBDD();
            List<String> usedImages = userBDD.getUsedSoldierImages();

            // Rechercher une image non utilisée
            for (String image : images) {
                if (!usedImages.contains(image)) {
                    return image; // Retourne la première image disponible
                }
            }

            // Si toutes les images sont utilisées, attribuer une image par défaut
            System.out.println("Toutes les images sont déjà attribuées. Attribution par défaut.");
            return "images/soldats/soldat_gris.jpg";

        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, attribuer une image par défaut
            return "images/soldats/soldat_gris.jpg";
        }
    }

}
