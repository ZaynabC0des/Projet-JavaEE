<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="js/gameControls.js"></script> 
<script src="js/modalControls.js"></script> 
<link rel="stylesheet" type="text/css" href="css/combat.css">


<%@ page import="model.Combat" %>
<%@ page import="java.util.Random" %>
<%@ page import="model.Soldat" %>
<%@ page import="java.util.List" %>

<script>

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
</script>

<% if (session.getAttribute("askDestroyForest") != null && (Boolean) session.getAttribute("askDestroyForest")) {
    String forestPos = (String) session.getAttribute("forestPosition");
%>
<script>


    if (confirm("Détruire la forêt en position <%= forestPos %> ?")) {
        window.location.href = "UpdatePositionServlet?action=destroyForest&position=<%= forestPos %>";
    } else {
        // Si l'utilisateur clique sur "Annuler", on met à jour sa position sans détruire la forêt
        window.location.href = "UpdatePositionServlet?action=moveOnly&position=<%= forestPos %>";
    }
</script>
<%
    session.setAttribute("askDestroyForest", false); // Réinitialiser l'indicateur
}
%>
<html>


<%
    List<Soldat> soldats = (List<Soldat>) session.getAttribute("soldats");
    if (soldats != null) {
        for (Soldat soldat : soldats) {
            //out.println("Soldat ID : " + soldat.getIdSoldat() + ", Points de vie : " + soldat.getPointDeVie() + "<br>");
        }
    }
%>


<% 
String warningMessage = (String) session.getAttribute("warningMessage");
if (warningMessage != null) {
%>
<div class="warning">
    <p><%= warningMessage %></p>
</div>
<%
    session.removeAttribute("warningMessage"); // Supprime le message après l'affichage
}
%>


<script>
        window.onload = function() {
            <% if (session.getAttribute("errorMessage") != null) { %>
                alert('<%= session.getAttribute("errorMessage") %>');  // Utilise JavaScript pour montrer une alerte
                <% session.removeAttribute("errorMessage"); %>  // Nettoie l'erreur après l'affichage
            <% } %>
        }

 window.onload = function() {
            // Vérifier si le popup doit être affiché
            <% if (Boolean.TRUE.equals(session.getAttribute("showPopup"))) { %>
                alert('Mouvement bloqué : la montagne en position  n\'est pas franchissable.');
                <% session.removeAttribute("showPopup"); // Supprime l'attribut après affichage %>
            <% } %>
        }
    </script>

<head>
     <title>Visualisation de la première carte</title>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="css/maps.css"> <!-- Assurez-vous que le chemin est correct -->
</head>
<body>
<div class="header">
    <button class="logout-button" id="logout-button">Déconnexion</button>
</div>

<div id="logoutModal" class="modal">
    <div class="modal-content">
        <span class="close" id="close">&times;</span>
        <p>Êtes-vous sûr de vouloir quitter le jeu ? Les données seront sauvegardées.</p>
        <button id="confirmLogout" class="confirm-button">Oui</button>
        <button id="cancelLogout" class="cancel-button">Annuler</button>
    </div>
</div>

<p>Position actuelle du joueur : <%= session.getAttribute("playerPosition") %></p>

<div class="movement-controls">
    <button onclick="movePlayer('up')">Haut</button>
    <button onclick="movePlayer('down')">Bas</button>
    <button onclick="movePlayer('left')">Gauche</button>
    <button onclick="movePlayer('right')">Droite</button>
</div>
<div class = "container">
<table border="1">
<%
String userFilePath = (String) session.getAttribute("userFilePath");
if (userFilePath == null) {
    out.println("<p>Erreur : Aucun fichier CSV associé à l'utilisateur.</p>");
} else {
    File csvFile = new File(userFilePath);
    if (csvFile.exists()) {
        // Récupérer la position actuelle du joueur
        String playerPosition = (String) session.getAttribute("playerPosition");
        int playerX = 0, playerY = 0;
        if (playerPosition != null) {
            String[] positionParts = playerPosition.split(",");
            playerX = Integer.parseInt(positionParts[0]);
            playerY = Integer.parseInt(positionParts[1]);
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int rowNum = 0; // Ajoutez un compteur pour les lignes
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                out.println("<tr>");
                for (int colNum = 0; colNum < values.length; colNum++) {
                    String value = values[colNum].trim();
                    int code = Integer.parseInt(value);
                    TuileType tuileType = TuileType.fromCode(code);
                    String imagePath = "";
                    switch (tuileType) {
                        case VIDE:
                            imagePath = "images/tuiles/vide.png";
                            break;
                        case VILLE:
                            imagePath = "images/tuiles/ville.png";
                            break;
                        case FORET:
                            imagePath = "images/tuiles/foret.png";
                            break;
                        case MONTAGNE:
                            imagePath = "images/tuiles/montagne.png";
                            break;
                        case SOLDAT: 
                            imagePath = "images/tuiles/soldat.png";
                            break;
                        default:
                            imagePath = "";
                    }
                    if (rowNum == playerX && colNum == playerY) {
                        out.println("<td>J1</td>");
                    } else if (code == 4) { // Code pour le soldat "S1"
                        out.println("<td>S1</td>");
                    } else {
                        out.println("<td><img src='" + request.getContextPath() + "/" + imagePath + "' style='width:50px; height:50px;'></td>");
                    }
                }
                out.println("</tr>");
                rowNum++;
            }
        } catch (IOException e) {
            out.println("</table>");
            out.println("<p>Erreur lors du chargement du fichier CSV : " + e.getMessage() + "</p>");
        }

        } %>
</table>
</div>

<% 
    Boolean canRecruit = (Boolean) session.getAttribute("canRecruit");
    if (canRecruit != null && canRecruit) {
%>
    <div class="recruit-button-container">
        <form action="RecruitSoldierServlet" method="POST">
            <button type="submit" class="recruit-button">Recruter un soldat</button>
        </form>
    </div>
<% 
    } 
%>


  <% if (Boolean.TRUE.equals(session.getAttribute("combatActive"))) { %>
<div class="combat-container">
        <h1>Fight</h1>
        <% if (combat.estCibleEnVie()) { %>
            <form method="post">
                <button type="submit" name="attaquer" class="combat-action-button">Attack</button>
            </form>
        <% } else { %>
            <form method="post">
                <button type="submit" name="reset" class="combat-action-button">Try again</button>
            </form>
        <% } %>

        <div class="result">
            <% if (lancerDe > 0) { %>

                <img src="./images/des/<%= lancerDe %>blanc.png" alt="Dé face <%= lancerDe %>" />
            <% } %>

            <% if (!message.isEmpty()) { %>
                <p><%= message %></p>
            <% } %>

            <% if (combat.estCibleEnVie()) { %>
            <%
                int totalDefense = 20; // Points de défense totaux de la cible
                int remainingDefense = combat.getPointsDefenseCible(); // Points restants
                int percentage = (int) ((double) remainingDefense / totalDefense * 100); // Calculer le pourcentage
            %>

            <div class="progress-bar">
                <div class="progress-bar-inner" style="width: <%= percentage %>%;"></div>
            </div>
            <p>Remaining defense pts: <%= remainingDefense %></p>
           <% } %>
           <% } %>
         <% } %>
          
           
        </div>
    </div>
</body>
</html> 
