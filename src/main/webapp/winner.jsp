<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gagnant de la partie</title>
    <link rel="stylesheet" href="./css/winner.css"> 
  
</head>
<body>
    <div class="winner-container">
        <h1>🎉 Gagnant de la partie 🎉</h1>
        <h2 id="winner-name">Chargement...</h2>
    </div>
    <script>
        fetch('winner')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById("winner-name").textContent = data.topPlayer;
                } else {
                    document.getElementById("winner-name").textContent = "Erreur : " + data.message;
                }
            })
            .catch(error => {
                console.error("Erreur lors de la requête : ", error);
                document.getElementById("winner-name").textContent = "Erreur lors du chargement.";
            });
    </script>
</body>
</html>
