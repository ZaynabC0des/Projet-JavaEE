<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="js/gameControls.js"></script> 
<script src="js/modalControls.js"></script> 
<script src="js/moveSoldat.js"></script> 
<link rel="stylesheet" type="text/css" href="css/combat.css">


<%@ page import="model.Combat" %>
<%@ page import="java.util.Random" %>
<%@ page import="model.Soldat, model.SoldatBDD" %>
<%@ page import="java.util.List" %>


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
<!DOCTYPE html>
<html>

<%
String soldierImage = (String) session.getAttribute("soldierImage");
%>
<img src="<%= soldierImage %>" alt="Soldat" class="soldier-image">


<img src="images/<%= soldierImage %>" alt="Soldat" class="soldier-image">

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

<% 
    Boolean canRecruit = (Boolean) session.getAttribute("canRecruit");
    if (canRecruit != null && canRecruit) {
%>
      <div class="button-container">
        <form action="RecruitSoldierServlet" method="POST">
            <button type="submit" class="custom-button">Recruter un soldat</button>
        </form>
    </div>
<% 
    } 
%>


<div class = "container">
<%
//Charger les soldats du joueur
    SoldatBDD soldatBDD = new SoldatBDD();
    String userLogin = (String) session.getAttribute("userLogin");
   
    List<Soldat> soldats = soldatBDD.getAllSoldats();
    
//charger le fichier csv
String userFilePath = (String) session.getAttribute("userFilePath");
if (userFilePath == null) {
    out.println("<p>Erreur : Aucun fichier CSV associé à l'utilisateur.</p>");
} else {
    File csvFile = new File(userFilePath);
    if (csvFile.exists()){
    	 // Récupérer la position actuelle du joueur
        String playerPosition = (String) session.getAttribute("playerPosition");
        int playerX = 0, playerY = 0;
        if (playerPosition != null) {
            String[] positionParts = playerPosition.split(",");
            playerX = Integer.parseInt(positionParts[0]);
            playerY = Integer.parseInt(positionParts[1]);
        }
  %>
  <script>
    function selectSoldat(soldatId) {
        console.log("Soldat " + soldatId); // Affiche l'ID du soldat dans la console
    }
</script>
  
<table border="1">
<% 
try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
    String line;
    int rowNum = 0;
    while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        out.println("<tr>");
        for (int colNum = 0; colNum < values.length; colNum++) {
            int code = Integer.parseInt(values[colNum].trim());
            String imagePath = "";

            // Vérifiez si un soldat est à cette position
            Soldat currentSoldat = null;
            for (Soldat s : soldats) {
                if (s.getX() == rowNum && s.getY() == colNum) {
                    currentSoldat = s;
                    imagePath = s.getImagePath(); // Récupérer le chemin de l'image 
                    break; // Arrêtez la boucle dès que le soldat est trouvé
                }
            }

            // Ajouter "J1" si c'est la position du joueur
            if (rowNum == playerX && colNum == playerY) {
                out.println("<td>J1</td>");
            } 
            
            else if (currentSoldat != null) {
                // Afficher un soldat
                out.println("<td style='position: relative;'>");
                out.println("<img src='" + request.getContextPath() + "/images/tuiles/vide.png' style='width: 50px; height: 50px; position: absolute;'>");
                out.println("<img src='" + request.getContextPath() + "/" + imagePath +"' style='width: 50px; height: 50px; position: relative; cursor: pointer;' onclick='selectSoldat(" + currentSoldat.getId() + ")'>");
                out.println("</td>");
            } else {
                // Afficher la case normale
                switch (code) {
                    case 0: // Case vide
                        imagePath = "images/tuiles/vide.png";
                        break;
                    case 1: // Ville
                        imagePath = "images/tuiles/ville.png";
                        break;
                    case 2: // Forêt
                        imagePath = "images/tuiles/foret.png";
                        break;
                    case 3: // Montagne
                        imagePath = "images/tuiles/montagne.png";
                        break;
                }
                out.println("<td><img src='" + request.getContextPath() + "/" + imagePath + "' style='width:50px; height:50px;'></td>");
            }
        }
        out.println("</tr>");
        rowNum++;
    }
} catch (IOException e) {
    out.println("</table>");
    out.println("<p style='color:red;'>Erreur lors du chargement du fichier CSV : " + e.getMessage() + "</p>");
}
}
}
%>
</table>

</div>


<script>
//Déclare une variable globale pour stocker l'ID du soldat sélectionné
window.selectedSoldatId = null;


// Fonction appelée lorsqu'un soldat est cliqué
function selectSoldat(soldatId) {
    selectedSoldatId = soldatId;
    alert("Soldat sélectionné ! ID : " + selectedSoldatId);
}

function moveSoldat(direction) {
	console.log("ID du soldat sélectionné :", window.selectedSoldatId); // Vérifie que l'ID est bien stocké
    if (!window.selectedSoldatId) {
        alert("Aucun soldat sélectionné !");
        return;
    }

    console.log("Déplacement du soldat ID :", window.selectedSoldatId, "Direction :", direction);

    fetch('MoveSoldatServlet?soldatId=' + window.selectedSoldatId + '&direction=' + direction)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log("Soldat déplacé !");
                location.reload(); // Recharge la carte après déplacement
            } else {
                console.error("Erreur du serveur : ", data.message);
                alert("Impossible de déplacer le soldat : " + data.message);
            }
        })
        .catch(error => console.error("Erreur lors de la requête : ", error));
}


//Écoute les touches "z", "q", "s", "d"
document.addEventListener('keydown', function(event) {
    let direction = null;

    switch (event.key) {
        case "z": // Haut
            direction = "up";
            break;
        case "s": // Bas
            direction = "down";
            break;
        case "q": // Gauche
            direction = "left";
            break;
        case "d": // Droite
            direction = "right";
            break;
        default:
            return; // Ignore les autres touches
    }

    if (direction) {
        moveSoldat(direction);
    }
});
</script>


               <!-- Bouton View Profile -->
		<button id="viewProfileButton" class="view-profile-button">View Profile</button>
            <!-- Profil Joueur -->
	   <div class="player-profile" id="playerProfile">
	       <h2>Your Profile</h2>
	       <p>Pseudo: <%= session.getAttribute("userLogin") %></p>
	       <p>Production points: <%= session.getAttribute("productionPoints") %></p>
	       <p>Nb of Soldiers: <%= session.getAttribute("nombreSoldats") %></p>
	       <p>Nb of Cities: <%= session.getAttribute("nombreVilles") %></p>
	       
	       
	   </div>
	
	   <!-- Script JavaScript directement intégré -->
	  <script>
    document.addEventListener("DOMContentLoaded", function () {
        const viewProfileButton = document.getElementById("viewProfileButton");
        const playerProfile = document.getElementById("playerProfile");

        viewProfileButton.addEventListener("click", function () {
            // Ajoute ou supprime la classe 'show' pour afficher/cacher le profil
            playerProfile.classList.toggle("show");
        });
        
    });
</script>
           
        </div>
    </div>
</body>
</html> 