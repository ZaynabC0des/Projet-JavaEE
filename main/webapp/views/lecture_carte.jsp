<!DOCTYPE html>
<html>

<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="../js/modalControls.js"></script> 
<script src="../js/moveSoldat.js"></script> 
<script src="../js/timer.js" defer></script>
<link rel="stylesheet" type="text/css" href="../css/combat.css">
<%@ page import="model.TuileType" %>
<%@ page import="java.io.*" %>  <!-- Importation de HttpSession -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Combat" %>
<%@ page import="java.util.Random" %>
<%@ page import="model.Soldat, model.SoldatBDD" %>
<%@ page import="controller.GameWebSocket" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="model.VilleBDD" %>
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

        let url = "ws://localhost:8080/projet/game/" + myUsername+"/"+code;
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
            case "respondTour":
                afficherTourActuel(data.username); // Afficher une alerte pour indiquer le tour
                break;
            case "playerLeft":
                // data.username
                onPlayerLeft(data.username);
                break;
            case "move":
                location.reload();
                break;
            
            default:
                console.warn("Type de message inconnu :", data.type);
        }
    }


     ///////////afficherTour   
    function afficherTourActuel(username) {
        if (username === myUsername) {
            alert("C'est votre tour !");
        } else {
            alert("C'est au tour de : " + username);
        }
    }
    
 // Mettre à jour l'interface quand un joueur rejoint
    function onPlayerJoined(username, score) {
        appendLog("Le joueur " + username + " a rejoint la partie. Score=" + score + ".");
    }


///////// Mettre à jour l'interface quand un joueur quitte
function onPlayerLeft(username) {
    appendLog("Le joueur " + username + " a quitté la partie.");
    // new
    const tempMessage = document.createElement("div");
    tempMessage.classList.add("temp-message");
    tempMessage.textContent = "Le joueur " + username + " a quitté la partie.";

    document.body.appendChild(tempMessage);

    setTimeout(() => {tempMessage.classList.add("hide");}, 4000);
    setTimeout(() => {document.body.removeChild(tempMessage);}, 5000);
}


///////////recruitSoldier
async function recruterSoldatAjax() {
  const messageContainer = document.getElementById('message-container');
  messageContainer.innerHTML = ''; // Effacer les messages précédents

  try {
      const response = await fetch('../RecruitSoldierServlet', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
      });
      const data = await response.json();

      if (data.success) {
          showPopup(data.message, 'success');
          setTimeout(() => {location.reload();}, 5000);
      } else {
          showPopup(data.message, 'error');
      }

  } catch (error) {
  	console.error('Erreur lors de la communication avec le serveur :', error);
      showPopup('Erreur de communication avec le serveur.', 'error');
  }
  
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
    
   <!-- POP UP  --> 
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
function showPopup(message, type) {
    Swal.fire({
        toast: true,
        position: 'top-end', // Position : haut à droite
        icon: type,          // 'success' ou 'error'
        title: message,
        showConfirmButton: false, // Pas de bouton "OK"
        timer: 5000,              // Disparaît après 2 secondes
        timerProgressBar: true    // Affiche une barre de progression
        
    });
}

</script>

<head>
     <title>Visualisation de la première carte</title>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="../css/maps.css"> <!-- Assurez-vous que le chemin est correct -->
</head>
<body>
<script>
    // Vérifier si le timer doit être réinitialisé
    const resetTimer = <%= session.getAttribute("resetTimer") != null ? "true" : "false" %>;

    // Retirer l'attribut après utilisation pour éviter une réinitialisation non voulue
    <% session.removeAttribute("resetTimer"); %>;
</script>
<div id="timer-container">Temps restant : 05:00</div>

   <script>
   function healSelectedSoldier() {
	    if (!window.selectedSoldatId) {
	        alert("Aucun soldat sélectionné !");
	        return;
	    }

	    fetch('../SoldierServlet', {
	        method: 'POST',
	        headers: {
	            'Content-Type': 'application/x-www-form-urlencoded'
	        },
	        body: 'soldatId=' + window.selectedSoldatId
	    })
	    .then(response => response.json())
	    .then(data => {
	        if (data.success) {
	            alert(data.message);
	            location.reload(); // Recharge la page pour voir les changements
	        } else {
	            alert(data.message);
	        }
	    })
	    .catch(error => {
	        console.error("Erreur lors de la requête :", error);
	        alert("Une erreur est survenue lors du soin du soldat.");
	    });
	}


   </script>
<%
    String errorMessage = (String) session.getAttribute("errorMessage");
    if (errorMessage != null) {
    	System.out.println("Error Message: " + errorMessage); // Log pour vérifier
%>
    <p style="color: red;"><%= errorMessage %></p>
<%
        session.removeAttribute("errorMessage");  // Nettoyer après affichage
    }
%>


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
  <table border="1"><%
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
                out.println("<img src='" + request.getContextPath() + "/" + imagePath +
                	    "' style='width: 50px; height: 50px; position: relative; cursor: pointer;' " +
                	    "onclick='selectSoldat(" + currentSoldat.getId() + ", " + currentSoldat.getPointDeVie() + ")'>");

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
function selectSoldat(soldatId, pointsDeVie) {
    if (soldatBlocked) {
        alert("Ce n'est pas votre tour !");
        return;
    }
    alert("Soldat sélectionné : " + soldatId + "\nPoints de vie : " + pointsDeVie);
    window.selectedSoldatId = soldatId;
}

function moveSoldat(direction) {
	console.log("ID du soldat sélectionné :", window.selectedSoldatId); // Vérifie que l'ID est bien stocké
    if (!window.selectedSoldatId) {
        alert("Aucun soldat sélectionné !");
        return;
    }

    console.log("Déplacement du soldat ID :", window.selectedSoldatId, "Direction :", direction);

    fetch('../MoveSoldatServlet?soldatId=' + window.selectedSoldatId + '&direction=' + direction)
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

document.addEventListener('keydown', function(event) {
    let direction = null;

    switch (event.key) {
        
        case "z":
        case "Z":
            direction = "up";
            break;
        
        case "s":
        case "S":
            direction = "down";
            break;
       
        case "q":
        case "Q":
            direction = "left";
            break;
        case "d":
        case "D":
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
  <%Integer score = (Integer) session.getAttribute("score");%>
       <div class="score-container">
           <h3>Your Score</h3>
           <p><%= score %></p>
       </div>
  <%String soldierImage = (String) session.getAttribute("soldierImage");%>


<!-- Affichage -->

<%
Integer lancerDe = (Integer) session.getAttribute("lancerDe"); // Récupérer lancerDe depuis la session
if (lancerDe == null) {
    lancerDe = 0; // Valeur par défaut
}
%>

<div class="combat-container">
    <h1>Fight</h1>
    <% 
    Integer damagePoints = (Integer) session.getAttribute("damagePoints"); // Récupère les points de dommage
    String combatOwner = (String) session.getAttribute("combatOwner");
    Integer remainingDefense = (Integer) session.getAttribute("combatDefensePoints"); // Défense restante
    String combatMessage = (String) session.getAttribute("combatMessage"); // Message du combat
   
    %>
    
    <% if (combatMessage != null) { %>
        <% if (combatMessage.equals("Ville capturée par " + combatOwner)) { %>
            <p>La ville a été capturée par <%= combatOwner %> !</p>
        <% 
        
        } else if (remainingDefense != null && remainingDefense > 0) { %>
            <p>Combat en cours...</p>
            <p>Points de dégâts : <%= damagePoints != null ? damagePoints : 0 %></p>
            
       <div class="result">
    <% if (lancerDe != null && lancerDe >= 1 && lancerDe <= 6) { %>
       <img src="<%= request.getContextPath() %>/images/des/<%= lancerDe %>blanc.png" alt="Dé face <%= lancerDe %>" />
        <% } %>
</div>

        <p>Defense remaining: <%= remainingDefense != null ? remainingDefense : 0 %></p>
        <div class="progress-bar">
            <div class="progress-bar-inner" style="width: <%= remainingDefense != null ? remainingDefense : 0 %>%;"></div>
        </div>
    <% } %>
<% } else { %>
    <p>Pas de combat en cours. Le soldat n'est pas sur une ville.</p>
<% } %>
</div>

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
         <div class="button-container">
         <button onclick="recruterSoldatAjax()" id="recruitSoldier" class="custom-button">Recruter un soldat</button>
     </div>
     <div id="message-container"></div>
          <div>
              <button onclick="healSelectedSoldier()" class="recruit-button">Soigner le Soldat Sélectionné</button>
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