// Sélectionner l'élément du timer
const timerContainer = document.getElementById('timer-container');

// Fonction pour formater les secondes en mm:ss
function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

// Fonction principale pour gérer le timer
function startTimer() {
    const now = new Date().getTime(); // Heure actuelle
    let endTime = localStorage.getItem('endTime');

    // Si aucun endTime enregistré, créer un nouveau timer de 5 minutes
    if (!endTime) {
        endTime = now + 0.1 * 60 * 1000; // 5 minutes (en millisecondes)
        localStorage.setItem('endTime', endTime); // Stocker dans localStorage
    } else {
        endTime = parseInt(endTime, 10);
    }

    // Calculer le temps restant immédiatement
    const currentTime = new Date().getTime();
    let timeLeft = Math.floor((endTime - currentTime) / 1000); // Temps restant en secondes

    // Afficher le temps initial dès le chargement
    if (timeLeft > 0) {
        timerContainer.textContent = `Temps restant : ${formatTime(timeLeft)}`;
    } else {
        timerContainer.textContent = "Temps écoulé !";
    }

    // Mettre à jour le timer toutes les secondes
    const timerTask = setInterval(() => {
        const currentTime = new Date().getTime(); // Heure actuelle
        timeLeft = Math.floor((endTime - currentTime) / 1000); // Temps restant en secondes

        if (timeLeft <= 0) {
            clearInterval(timerTask); // Arrêter le timer
            localStorage.removeItem('endTime'); // Nettoyer le localStorage
            timerContainer.textContent = "Temps écoulé !";
            window.location.href = "winner.jsp"; // Redirige vers une nouvelle page JSP

        } else {
            timerContainer.textContent = `Temps restant : ${formatTime(timeLeft)}`;
        }
    }, 1000); // Mettre à jour toutes les secondes
}

// Lancer le timer
startTimer();