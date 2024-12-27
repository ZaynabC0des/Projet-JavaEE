package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.UserBDD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String login = request.getParameter("login");
	    String password = request.getParameter("password");
	    User u1 = new User(login, password);
	    UserBDD utable = new UserBDD();
	    PrintWriter out = response.getWriter();
	    
	    try {
	        //User foundUser = utable.findUser(u1);
	        if (utable.findUser(u1) != null) {
	            out.println("Bienvenue dans notre jeu " + u1.getLogin());
	        } else {
	            out.println("Utilisateur non trouvé.");
	        }
	    } catch (SQLException e) {
	        out.println("Erreur de connexion à la base de données: " + e.getErrorCode() + " : " + e.getMessage());
	        if (e.getErrorCode() == 0) { // Assurez-vous que le code d'erreur est correct pour une connexion échouée
	            System.out.println("Pas connecté à la BDD");
	        }
	    }
	}


}
