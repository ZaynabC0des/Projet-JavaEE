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
        case "ArrowUp": // Haut
            direction = "up";
            break;
        case "ArrowDown": // Bas
            direction = "down";
            break;
        case "ArrowLeft": // Gauche
            direction = "left";
            break;
        case "ArrowRight": // Droite
            direction = "right";
            break;
        default:
            return; // Ignore les autres touches
    }

    if (direction) {
        moveSoldat(direction);
    }
});