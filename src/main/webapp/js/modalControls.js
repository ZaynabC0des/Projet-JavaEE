document.addEventListener('DOMContentLoaded', function() {
    var logoutButton = document.getElementById('logout-button');
    var logoutModal = document.getElementById('logoutModal');
    var closeModal = document.getElementById('close');
    var confirmLogout = document.getElementById('confirmLogout');
    var cancelLogout = document.getElementById('cancelLogout');

    logoutButton.onclick = function() {
        logoutModal.style.display = 'block';
    };
    closeModal.onclick = function() {
        logoutModal.style.display = 'none';
    };
    confirmLogout.onclick = function() {
        window.location.href = 'connexion.jsp';
    };
    cancelLogout.onclick = function() {
        logoutModal.style.display = 'none';
    };
    window.onclick = function(event) {
        if (event.target === logoutModal) {
            logoutModal.style.display = 'none';
        }
    };
});


 function selectSoldat(soldatId) {
        // Envoyer l'ID du soldat au serveur pour le sélectionner
        fetch('SelectSoldatServlet?soldatId=' + soldatId)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("Soldat sélectionné ! Utilisez les boutons de déplacement.");
                } else {
                    alert("Erreur lors de la sélection du soldat.");
                }
            });
    }