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
    System.out.println("Erreur : la grille n'est pas initialisée.");
    response.getWriter().println("Erreur : la grille n'est pas initialisée.");
    return;
}

//position du joueur 
String pos = (String) session.getAttribute("playerPosition");
if (pos == null) {
    pos = "0,0";
    session.setAttribute("playerPosition", pos);
}

//pour obtenir les coordonnées 
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
 session.setAttribute("playerPosition", newX + "," + newY); //mise a jours du joueur pour la ville 
if (grille[newX][newY] == 3) {
	session.setAttribute("showPopup", true); 
    System.out.println("Mouvement bloqué : la montagne en position (" + newX + ", " + newY + ") n'est pas franchissable.");
    response.sendRedirect("lecture_carte.jsp"); // Redirige vers la mï¿½me page sans changer la position   
} 
else if (grille[newX][newY] == 2) { // Vérification si la case est une forêt
    session.setAttribute("proposedPosition", newX + "," + newY);
    session.setAttribute("askDestroyForest", true); // Demande de confirmation pour détruire la forêt
    session.setAttribute("forestPosition", newX + "," + newY); // Sauvegarde de la position de la forêt
    System.out.println("Arrivée sur une case forêt en position (" + newX + ", " + newY + ")");
    response.sendRedirect("lecture_carte.jsp");
} 

else {
    session.setAttribute("askDestroyForest", false); // Aucune forêt rencontrée
    session.setAttribute("playerPosition", newX + "," + newY); // Mise à jour de la position
    if (grille[newX][newY] == 0) {
        System.out.println("Position mise à jour : (" + newX + ", " + newY + ")");
    } else {
        System.out.println("Mouvement bloqué : la tuile (" + newX + ", " + newY + ") n'est pas accessible.");
    }
    response.sendRedirect("lecture_carte.jsp");
        }
    }
}
