<!DOCTYPE html>
<html>

<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="js/modalControls.js"></script> 
<script src="js/moveSoldat.js"></script> 
<script src="timer.js" defer></script>

<link rel="stylesheet" type="text/css" href="css/combat.css">


<%@ page import="model.Combat" %>
<%@ page import="java.util.Random" %>
<%@ page import="model.Soldat, model.SoldatBDD" %>
<%@ page import="controller.GameWebSocket" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.SQLException" %>

<%@ page session="true" %>
<%
    // Supposons qu'on ait stocké le pseudo en session
    String username = (String) session.getAttribute("userLogin");
    if (username == null) {
        // Si pas connecté, redirection
        response.sendRedirect("connexion.jsp");
        return;
    }
%>


<script>
    let soldatBlocked=false;
    let ws;
    let myUsername = "<%= username %>";
    let code="<%= session.getAttribute("code") %>";

    function appendLog(msg) {

        console.log("Message : " + msg);
    }

    function initWebSocket() {

        let url = "ws://localhost:8080/projet_war_exploded/game/" + myUsername+"/"+code;
        ws = new WebSocket(url);

        ws.onopen = function() {
            ws.send("{\"type\":\"askTour\",\"username\":\""+myUsername+"\"}");
        };

        ws.onmessage = function(event) {


            // Tenter de parser le message JSON
            try {

                let data = JSON.parse(event.data);
                if(data.code!==code){
                    return;
                }
                appendLog("[WS] Message reçu : " + event.data);
                handleMessage(data);



            } catch(e) {
                console.error("Erreur parsing JSON", e);
            }
        };

        ws.onclose = function() {
            ws.send(myUsername+" vient de se déconnecter");
        };

        ws.onerror = function(error) {
            appendLog("[WS] Erreur : " + error);
        };
    }

    // Gérer les différents types de messages reçus
    function handleMessage(data) {
        switch(data.type) {
            case "playerJoined":

                onPlayerJoined(data.username, data.score);
                break;
            case "playerLeft":
                // data.username
                onPlayerLeft(data.username);
                break;
            case "move":
                location.reload();
                break;
            case "respondTour":
                if (data["username"]===myUsername){
                    soldatBlocked=false;
                } else {
                    soldatBlocked=true;
                }
                break;
            default:
                console.warn("Type de message inconnu :", data.type);
        }
    }

    // Mettre à jour l'interface quand un joueur rejoint
    function onPlayerJoined(username, score) {
        appendLog("Le joueur " + username + " a rejoint la partie. Score=" + score + ".");

    }

    // Mettre à jour l'interface quand un joueur quitte
    function onPlayerLeft(username) {
        appendLog("Le joueur " + username + " a quitté la partie.");

    }

    initWebSocket();



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
<script>
    // Vérifier si le timer doit être réinitialisé
    const resetTimer = <%= session.getAttribute("resetTimer") != null ? "true" : "false" %>;

    // Retirer l'attribut après utilisation pour éviter une réinitialisation non voulue
    <% session.removeAttribute("resetTimer"); %>;
</script>
<div id="timer-container">Temps restant : 05:00</div>


<div class = "container">
<%
//Charger les soldats du joueur
    SoldatBDD soldatBDD = new SoldatBDD();
    String userLogin = (String) session.getAttribute("userLogin");

    List<Soldat> soldats = null;
    try {
        soldats = soldatBDD.getAllSoldats((String)session.getAttribute("code"));
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }

//charger le fichier csv
String userFilePath = (String) session.getAttribute("gameFilePath");
if (userFilePath == null) {
    out.println("<p>Erreur : Aucun fichier CSV associé à l'utilisateur.</p>");
} else {
    File csvFile = new File(userFilePath);


  %>

  
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


            
            if (currentSoldat != null) {
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
%>
</table>

</div>


<script>
//Déclare une variable globale pour stocker l'ID du soldat sélectionné
window.selectedSoldatId = null;


// Fonction appelée lorsqu'un soldat est cliqué
function selectSoldat(soldatId) {
    if(soldatBlocked){
        alert("Ce n'est pas votre tour !");
        return;
    }

    alert("Soldat sélectionné : " + soldatId);
    window.selectedSoldatId = soldatId;





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
                ws.send("{\"type\":\"move\",\"username\":\""+myUsername+"\",\"soldatId\":"+window.selectedSoldatId+"}");

                location.reload(); // Recharge la carte après déplacement
            } else {
                console.error("Erreur du serveur : ", data.message);
                alert("Impossible de déplacer le soldat : " + data.message);
            }
        })
        .catch(error => console.error("Erreur lors de la requête : ", error));
}


//Écoute les touches fléchées
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

  <%
    Integer score = (Integer) session.getAttribute("score");
        %>
        <div class="score-container">
            <h3>Your Score</h3>
            <p><%= score %></p>
        </div>
	     <%String soldierImage = (String) session.getAttribute("soldierImage");%>

               <!-- Bouton View Profile -->
		<button id="viewProfileButton" class="view-profile-button">View Profile</button>
            <!-- Profil Joueur -->
	  <div class="player-profile" id="playerProfile">
    <h2>Your Profile</h2>
    <div class="soldier-container">
        <img src="<%= soldierImage %>" alt="Soldat" class="soldier-image">
    </div>

    <p>Pseudo: <%= (session.getAttribute("userLogin") != null) ? session.getAttribute("userLogin") : "Unknown" %></p>
    <p>Production points: <%= (session.getAttribute("productionPoints") != null) ? session.getAttribute("productionPoints") : "0" %></p>
    <p>Nb of Soldiers: <%= (session.getAttribute("nombreSoldats") != null) ? session.getAttribute("nombreSoldats") : "0" %></p>
    <p>Nb of Cities: <%= (session.getAttribute("nombreVilles") != null) ? session.getAttribute("nombreVilles") : "0" %></p>

    <%
        Boolean canRecruit = (Boolean) session.getAttribute("canRecruit");
        canRecruit = true;
        if (canRecruit != null && canRecruit) {
    %>
        <div>
            <form action="RecruitSoldierServlet" method="POST">
                <button type="submit" id="recruitSoldier" class="recruit-button">Recruter un soldat</button>
            </form>
        </div>
    <%
        }
    %>

    <div class="footer">
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
           

</body>
</html> 