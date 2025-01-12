<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Gestion des Sessions</title>
    <link rel="stylesheet" type="text/css" href="css/connexion.css">
</head>
<body>
    <h1>Créer ou Rejoindre une Partie</h1>

    <form action="session" method="POST">
        <input type="hidden" name="action" value="create">
        <label for="details">Détails de la session :</label>
        <input type="text" id="details" name="details" placeholder="Description de la session" required>
        <button type="submit">Créer une session</button>
    </form>

    <form action="session" method="POST" >
        <input type="hidden" name="action" value="join">
        <label for="code">Code de session :</label>
        <input type="text" id="code" name="code" placeholder="Code de la session" required>
        <button type="submit">Rejoindre une session</button>
    </form>
</body>
</html>
