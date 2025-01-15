package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.UserBDD;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/winner")
public class WinnerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    UserBDD userBDD = new UserBDD();
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");

	    try {
	        String topPlayer = userBDD.getTopPlayer();
	        System.out.println(topPlayer);
	        if (topPlayer == null) {
	            response.getWriter().write("{\"success\": false, \"message\": \"Aucun joueur trouvé.\"}");
	        } else {
	            response.getWriter().write("{\"success\": true, \"topPlayer\": \"" + topPlayer + "\"}");
	        }
	        return;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.getWriter().write("{\"success\": false, \"message\": \"Erreur interne.\"}");
	        return;
	    }
	}

}
