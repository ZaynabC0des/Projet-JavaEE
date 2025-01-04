function movePlayer(direction) {
    window.location.href = 'PlayerMoveServlet?direction=' + direction;
}
document.addEventListener('keydown', function(event) {
    console.log(event.key);  // Ajoutez cette ligne pour voir quelle touche est pressée
    switch (event.key) {
        case "ArrowUp":
            movePlayer('up');
            break;
        case "ArrowDown":
            movePlayer('down');
            break;
        case "ArrowLeft":
            movePlayer('left');
            break;
        case "ArrowRight":
            movePlayer('right');
            break;
        default:
            return; // Quitte cette fonction de gestion d'événements pour les touches non gérées
    }
    event.preventDefault(); // Empêche l'action par défaut pour ne pas faire défiler la page
});