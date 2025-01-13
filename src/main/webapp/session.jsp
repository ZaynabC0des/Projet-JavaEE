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
        <button type="submit">Créer une session</button>
    </form>
  <% if (request.getAttribute("message") != null) { %>
 	<div class="container">
    <h2>Code : <%= request.getAttribute("message") %></h2>
    </div>
     <% } if(request.getAttribute("message_erreur") != null) { %>
   	 <div class="container_erreur">
   	    <h2><%= request.getAttribute("message_erreur") %></h2>
   	    </div>

    <% }%>


    <form action="session" method="POST" >
        <input type="hidden" name="action" value="join">
        <label for="code">Code de session :</label>
        <input type="text" id="code" name="code" placeholder="Code de la session" required>
        <button type="submit">Rejoindre une session</button>
    </form>
</body>
</html>
