package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
@WebServlet("/PlayerMoveServlet")
public class PlayerMoveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
    HttpSession session = request.getSession();//toujours co a la session
int[][] grille = (int[][]) session.getAttribute("grille");//recupere la grille

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

if (grille[newX][newY] == 3) {
	session.setAttribute("showPopup", true); 
    System.out.println("Mouvement bloqu� : la montagne en position (" + newX + ", " + newY + ") n'est pas franchissable.");
    response.sendRedirect("lecture_carte.jsp"); // Redirige vers la m�me page sans changer la position
    
} else if (grille[newX][newY] == 2) { // V�rification si la case est une for�t
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
        System.out.println("Position mise � jour : (" + newX + ", " + newY + ")");
    } else {
        System.out.println("Mouvement bloqu� : la tuile (" + newX + ", " + newY + ") n'est pas accessible.");
    }
    response.sendRedirect("lecture_carte.jsp");
        }
    }
}