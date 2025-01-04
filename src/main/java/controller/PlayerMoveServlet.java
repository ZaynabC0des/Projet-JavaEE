package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Combat;
import model.UserBDD;
import model.VilleBDD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
@WebServlet("/PlayerMoveServlet")
public class PlayerMoveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
    HttpSession session = request.getSession();//toujours co a la session
    String userLogin = (String) session.getAttribute("userLogin");
    int[][] grille = (int[][]) session.getAttribute("grille");//recupere la grille
	VilleBDD villeBDD = new VilleBDD();
    UserBDD userBDD = new UserBDD();
    
	if (grille == null) { //les erreurs possible d initialisation
	    System.out.println("Erreur : la grille n'est pas initialis�e.");
	    response.getWriter().println("Erreur : la grille n'est pas initialis�e.");
	    return;
	}
	
	//position du joueur 
	String pos = (String) session.getAttribute("playerPosition");
	if (pos == null) {
	    pos = "0,0";
	    session.setAttribute("playerPosition", pos);
	}
	
	//pour obtenir les coordonn�es 
	String[] parts = pos.split(",");
	int x = Integer.parseInt(parts[0]);
	int y = Integer.parseInt(parts[1]);
	System.out.println("Position actuelle : (" + x + ", " + y + ")");
	
	//new position en fonction de la direction
	String direction = request.getParameter("direction");
	int newX = x;
	int newY = y;
	

	switch (direction) {
	    case "up":
	        if (x > 0) newX--;
	        break;
	    case "down":
	        if (x < grille.length - 1) newX++;
	        break;
	    case "left":
	        if (y > 0) newY--;
	        break;
	    case "right":
	        if (y < grille[0].length - 1) newY++;
	        break;
	    default:
	        System.out.println("Direction invalide : " + direction);
	        break;
	}
	
	//les positions du csv 
	//
	if (grille[newX][newY] == 3) {
		session.setAttribute("showPopup", true); 
		session.setAttribute("errorMessage", "Mouvement bloqu� : Montagne non franchissable.");
	    System.out.println("Mouvement bloqu� : la montagne en position (" + newX + ", " + newY + ") n'est pas franchissable.");
	    response.sendRedirect("lecture_carte.jsp"); // Redirige 
	    return; // Sortir de la m�thode pour emp�cher toute mise � jour
	}
	else if (grille[newX][newY] == 2) { // V�rification si la case est une for�t
	    session.setAttribute("proposedPosition", newX + "," + newY);
	    session.setAttribute("askDestroyForest", true); // Demande de confirmation pour d�truire la for�t
	    session.setAttribute("forestPosition", newX + "," + newY); // Sauvegarde de la position de la for�t
	    System.out.println("Arriv�e sur une case for�t en position (" + newX + ", " + newY + ")");
	    response.sendRedirect("lecture_carte.jsp");
	} 
	
	else {
	    session.setAttribute("askDestroyForest", false); // Aucune for�t rencontr�e
	    session.setAttribute("playerPosition", newX + "," + newY); // Mise � jour de la position
	    
    if (grille[newX][newY] == 0) {
        System.out.println("Position mise a jour : (" + newX + ", " + newY + ")");
    }
    session.setAttribute("playerPosition", newX + "," + newY); //mise a jours du joueur pour la ville 
    // D�claration et r�cup�ration de 'combat' de la session pour v�rifier s'il est null
   // Combat combat = (Combat) session.getAttribute("combat");

     if ( grille[newX][newY] == 1) { // Si on est sur une case ville
    	 try {
             int defensePoints = villeBDD.getCityDefensePoints(newX, newY);
             Combat combat = new Combat(defensePoints, "ville");
             session.setAttribute("combat", combat);
             System.out.println("Nouveau combat initi� avec " + defensePoints + " points de d�fense � la position (" + newX + ", " + newY + ")");

             if ("attaquer".equals(request.getParameter("action"))) {
                 Random random = new Random();
                 int lancerDe = random.nextInt(6) + 1;
                 String combatResult = combat.Attaque(lancerDe);
                 session.setAttribute("combatResult", combatResult);
                 session.setAttribute("lancerDe", lancerDe);

                 if (!combat.estCibleEnVie()) {
                     session.removeAttribute("combat");
                     System.out.println("Combat termin� � la position (" + newX + ", " + newY + ")");
                 }
             }
    	 }
        catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur SQL lors de la mise � jour des points de production : " + e.getMessage());
            response.getWriter().println("Erreur de base de donn�es lors de la mise � jour des points de production.");
        }
    

    }
    System.out.println("newX: " + newX + ", newY: " + newY);
    System.out.println("Valeur de grille � cette position: " + grille[newX][newY]);
    System.out.println("Param�tre 'attaquer': " + request.getParameter("attaquer"));
    
    response.sendRedirect("lecture_carte.jsp");
    

}
}
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
}

}