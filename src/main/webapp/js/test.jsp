<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Formulaire Nom et Pr�nom</title>
</head>
<body>
<form action="submitForm" method="POST">
    <div>
        <label for="prenom">Pr�nom:</label>
        <input type="text" id="prenom" name="prenom" required>
    </div>
    <div>
        <label for="nom">Nom:</label>
        <input type="text" id="nom" name="nom" required>
    </div>
    <div>
        <button type="submit">Soumettre</button>
    </div>
</form>
</body>
</html>
