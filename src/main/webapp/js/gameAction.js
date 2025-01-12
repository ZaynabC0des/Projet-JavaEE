// gameActions.js
function healSoldier() {
    if (!window.selectedSoldatId) {
        alert("Aucun soldat sélectionné !");
        return;
    }

    fetch(`HealSoldierServlet?soldatId=${window.selectedSoldatId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log("Soldat soigné !");
                alert("Votre soldat a été soigné.");
                location.reload(); // Rechargez éventuellement la page ou mettez à jour l'interface utilisateur en conséquence
            } else {
                console.error("Erreur lors de la tentative de soin : ", data.message);
                alert("Impossible de soigner le soldat : " + data.message);
            }
        })
        .catch(error => {
            console.error("Erreur de réseau : ", error);
            alert("Erreur de réseau lors de la tentative de soin.");
        });
}
