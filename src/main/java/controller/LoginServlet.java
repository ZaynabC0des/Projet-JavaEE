package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import model.UserBDD;
import java.io.IOException;
import java.sql.SQLException;


public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public LoginServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        User u1 = new User(login, password);
        UserBDD utable = new UserBDD();
      
        try {
            User foundUser = utable.findUser(u1);
            if (foundUser != null) {
                // Stocke l'objet User et son login dans la session
                session.setAttribute("user", foundUser);
                session.setAttribute("userLogin", foundUser.getLogin());
                int score = utable.getUserScore(foundUser.getLogin());
                session.setAttribute("score", score);
                System.out.println("Utilisateur trouve : " + foundUser.getLogin());
                // Récupérer les données supplémentaires de l'utilisateur
                User userDetails = utable.getUserDetails(foundUser.getLogin());
                if (userDetails != null) {
                	  session.setAttribute("productionPoints", userDetails.getPointProduction());
                      session.setAttribute("nombreVilles", utable.compterVillesPossedeesParUtilisateur(foundUser.getLogin()));
                 // Recuperer et stocker l'image du soldat dans la session
                    session.setAttribute("soldierImage", userDetails.getSoldierImage());
                
                }
                response.sendRedirect("session.jsp");
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



}
