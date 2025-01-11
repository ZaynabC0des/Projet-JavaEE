<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
     <title>Visualisation de la première carte</title>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="css/maps.css"> <!-- Assurez-vous que le chemin est correct -->
</head>
<body>

<div class="header">
    <div class="title-container">
        <h1>MAPS 1</h1>
    </div>
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

<% if (session.getAttribute("askCityPopup") != null && (Boolean) session.getAttribute("askCityPopup")) {
    String cityPosition = (String) session.getAttribute("cityPosition");
    String villeMessage = (String) session.getAttribute("villeMessage");
%>
<script>
    alert("<%= villeMessage %>");
    window.location.href = "UpdatePositionServlet?action=moveOnly&position=<%= cityPosition %>"; // Juste pour réinitialiser la position si nécessaire
</script>
<%
    session.setAttribute("askCityPopup", false); // Réinitialiser l'indicateur après affichage
}
%>


<table border="1">
<% 

    String userFilePath = (String) session.getAttribute("userFilePath");
    if (userFilePath == null) {
        out.println("<p>Erreur : Aucun fichier CSV associé à l'utilisateur.</p>");
    } else {
        File csvFile = new File(userFilePath);
        if (csvFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    out.println("<tr>");
                    for (String value : values) {
                        int code = Integer.parseInt(value.trim());
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
                            default:
                                imagePath = "";
                        }
                        out.println("<td><img src='" + request.getContextPath() + "/" + imagePath + "' style='width:50px; height:50px;'></td>");
                    }
                    out.println("</tr>");
                }
            } catch (IOException e) {
                out.println("</table>");
                out.println("<p>Erreur lors du chargement du fichier CSV : " + e.getMessage() + "</p>");
            }
        } else {
            out.println("</table>");
            out.println("<p>Fichier CSV non trouvé.</p>");
        }
    }
%>
</table>

<script>
    document.getElementById('logout-button').onclick = function() {
        document.getElementById('logoutModal').style.display = 'block';
    };

    document.getElementById('close').onclick = function() {
        document.getElementById('logoutModal').style.display = 'none';
    };

    document.getElementById('confirmLogout').onclick = function() {
        // Ajoutez ici la logique de déconnexion
        window.location.href = 'connexion.jsp'; // Simule la redirection après déconnexion
    };

    document.getElementById('cancelLogout').onclick = function() {
        document.getElementById('logoutModal').style.display = 'none';
    };

    // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        var modal = document.getElementById('logoutModal');
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    };
</script>


</body>
</html>
