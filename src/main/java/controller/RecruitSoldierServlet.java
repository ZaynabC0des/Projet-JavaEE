package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.SoldatBDD;
import model.UserBDD;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/RecruitSoldierServlet")
public class RecruitSoldierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userLogin = (String) session.getAttribute("userLogin");

        if (userLogin == null || userLogin.isEmpty()) {
            System.out.println("Erreur : L'utilisateur n'est pas authentifi� ou le login est invalide.");
            response.sendRedirect("connexion.jsp");
            return;
        }

        UserBDD userBDD = new UserBDD();
        try {
            int currentPoints = userBDD.getProductionPoints(userLogin);
            System.out.println("Points de production pour le joueur " + userLogin + ": " + currentPoints);
            
            if (currentPoints >= 15) {
                SoldatBDD soldatBDD = new SoldatBDD();
                int soldatId = soldatBDD.ajouterSoldatEtRecupererId(userLogin, 100);
                System.out.println("Tentative d'ajout d'un soldat pour " + userLogin);

                if (soldatId != -1) {
                    userBDD.updateProductionPoints(userLogin, -15);
                    int nombreSoldats = soldatBDD.compterSoldatsPossedesParUtilisateur(userLogin);
                    session.setAttribute("nombreSoldats", nombreSoldats);
                    System.out.println("Soldat ajout� avec succ�s avec l'ID: " + soldatId);
                    request.setAttribute("message", "Soldat recrut� avec succ�s, ID: " + soldatId);
                    request.getRequestDispatcher("lecture_carte.jsp").forward(request, response);
                } else {
                    request.setAttribute("error", "Erreur lors du recrutement du soldat.");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                }
            } else {
                System.out.println("Pas assez de points de production pour recruter un soldat.");
                request.setAttribute("error", "Pas assez de points de production pour recruter un soldat.");
                request.getRequestDispatcher("lecture_carte.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur de base de donn�es.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
