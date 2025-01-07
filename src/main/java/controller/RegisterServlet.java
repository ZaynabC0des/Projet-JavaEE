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

        User newUser = new User(login, password);
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
                String baseDir = "H:\\Documents\\ProgWeb\\Projet-JavaEE\\projet\\src\\main\\webapp\\maps";
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
                request.setAttribute("error", "L'inscription a �chou�. Erreur lors de l'ajout de l'utilisateur.");
                request.getRequestDispatcher("connexion.jsp").forward(request, response);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur de syst�me: " + e.getMessage());
            request.getRequestDispatcher("connexion.jsp").forward(request, response);
        }
    }
    
    
    //test 
    private String assignSoldierImage(String username) {
        int hash = username.hashCode();
        int index = Math.abs(hash % 4); // Limite � 5 images ou couleurs
        String[] images = {
            "images/soldats/soldat_bleu.jpg",
            "images/soldats/soldat_vert.jpg",
            "images/soldats/soldat_rouge.jpg",
            "images/soldats/soldat_violet.jpg"
           
        };
        return images[index];
    }

}
