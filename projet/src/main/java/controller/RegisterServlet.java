package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.UserBDD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RegisterServlet() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String repeatPassword = request.getParameter("psw-repeat");

        PrintWriter out = response.getWriter();
        if (!password.equals(repeatPassword)) {
            out.println("Les mots de passe ne correspondent pas.");
            return;
        }

        User newUser = new User(login, password);  
        UserBDD utable = new UserBDD();

        try {
            // Vérifie si l'utilisateur existe déjà
            if (utable.checkUserExists(login)) {
                out.println("L'inscription a échoué. Le login " + login + " est déjà utilisé.");
                return;
            }

            if (utable.addUser(newUser)) {
                out.println("Inscription réussie. Bienvenue " + login + "!");
            } else {
                out.println("L'inscription a échoué. Erreur lors de l'ajout de l'utilisateur.");
            }
        } catch (SQLException e) {
            out.println("Erreur de connexion à la base de données: " + e.getErrorCode() + " : " + e.getMessage());
        }
    }
}
