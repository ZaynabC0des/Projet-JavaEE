<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Combat" %>
<%@ page import="java.util.Random" %>

<%
    // Initialiser le combat s'il n'existe pas dans la session
    Combat combat = (Combat) session.getAttribute("combat");
    if (combat == null) {
        // Exemple : Combat contre une ville avec 10 points de défense
        combat = new Combat(13, "ville");
        session.setAttribute("combat", combat);
    }

    String message = "";
    int lancerDe = 0; 

    if (request.getParameter("attaquer") != null && combat.estCibleEnVie()) {
        Random random = new Random();
        lancerDe = random.nextInt(6) + 1; 
        message = combat.Attaque(lancerDe); 
        if (!combat.estCibleEnVie()) {
            session.removeAttribute("combat"); // Fin du combat, suppression de l'objet
        }
    } else if (request.getParameter("reset") != null) {
        // Réinitialiser le combat
        session.removeAttribute("combat");
        combat = new Combat(20, "ville"); 
        session.setAttribute("combat", combat);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Combat</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            background-color: #f4f4f9;
            margin: 0;
            padding: 0;
        }
        h1 {
            margin-top: 20px;
        }
        button {
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #007BFF;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }
        button:hover {
            background-color: #0056b3;
        }
        .result {
            margin-top: 30px;
        }
        img {
            margin-top: 20px;
            width: 100px;
            height: 100px;
            border: 2px solid #ddd;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body>
    <h1>Combat</h1>

    <% if (combat.estCibleEnVie()) { %>
        <form method="post">
            <button type="submit" name="attaquer">Attaquer</button>
        </form>
    <% } else { %>
        <form method="post">
            <button type="submit" name="reset">Recommencer le Combat</button>
        </form>
    <% } %>

    <div class="result">
        <% if (lancerDe > 0) { %>
            <p>Résultat dé : <%= lancerDe %></p>
            <img src="images/des/<%= lancerDe %>.png" alt="Dé face <%= lancerDe %>" />
        <% } %>

        <% if (!message.isEmpty()) { %>
            <p><%= message %></p>
        <% } %>

         <% if (combat.estCibleEnVie()) { %>
            <p>Points de défense restants : <%= combat.getPointsDefenseCible() %></p>
        <% } %> 
    </div>
</body>
</html>
