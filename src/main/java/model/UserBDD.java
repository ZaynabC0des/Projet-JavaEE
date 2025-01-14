package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserBDD {
	
	public Connection init() {
	    String url = "jdbc:mysql://localhost:3306/projet_jee";
	    String user = "root";
	    String password = "";
	    Connection cnx = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
        try {
	        cnx = DriverManager.getConnection(url, user, password);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return cnx;
	}
	
	//ajout d un utilisateur dans la bdd
	public boolean addUser(User u) throws SQLException {
	    String sql = "INSERT INTO user (login, password, soldierImage) VALUES (?, ?, ?)";
	    try (Connection cnx = this.init();
	         PreparedStatement stmt = cnx.prepareStatement(sql)) {
	        stmt.setString(1, u.getLogin());
	        stmt.setString(2, u.getPassword());
	        stmt.setString(3, u.getSoldierImage());
	        int result = stmt.executeUpdate();
	        return result > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw e; // Relance l'exception pour la g�rer plus haut dans la stack
	    }
	}

	//recherche de l utilisateur dans la bdd
	public User findUser(User u) throws SQLException {
		Connection cnx= this.init();
		Statement stm = cnx.createStatement();
		String sql = "SELECT * FROM user WHERE login = '" + u.getLogin() + "' AND password = '" + u.getPassword() + "'";
		ResultSet rs = stm.executeQuery(sql);
		if (rs.next()) 
			return new User(rs.getString(2), rs.getString(3));
		else 
			return null;
		
	}
	
	//verifier que le user existe
	public boolean checkUserExists(String login) throws SQLException {
	    String query = "SELECT count(*) FROM user WHERE login = ?";
	    try (Connection conn = this.init();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, login);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    }
	    return false;
	}
	
	public boolean updateProductionPoints(String login, int pointChange) throws SQLException {
        Connection cnx = null;
        PreparedStatement stmt = null;
        try {
            cnx = this.init();
            cnx.setAutoCommit(false); // Commencer une transaction

            // Vérification des points actuels pour s'assurer qu'ils ne deviendront pas négatifs
            int currentPoints = getProductionPoints(login);
            if (currentPoints + pointChange < 0) {
                cnx.rollback(); // Annuler la transaction si les points deviennent négatifs
                return false; // Indiquer que l'opération a échoué
            }

            String sql = "UPDATE user SET point_production = point_production + ? WHERE login = ?";
            stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, pointChange);
            stmt.setString(2, login);

            int result = stmt.executeUpdate();
            cnx.commit(); // Valider la transaction si tout va bien
            return result > 0;
        } catch (SQLException e) {
            if (cnx != null) {
                try {
                    cnx.rollback(); // S'assurer de faire un rollback en cas d'erreur
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Propager l'exception
        } finally {
            if (stmt != null) stmt.close();
            if (cnx != null) cnx.close();
        }
    }
	
	public int getProductionPoints(String login) throws SQLException {
	    String query = "SELECT point_production FROM user WHERE login = ?";
	    try (Connection conn = this.init();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, login);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("point_production");
	        }
	    }
	    return 0; // Valeur par d�faut si aucun utilisateur trouv�
	
	}

	public boolean checkProductionPoints(String login) throws SQLException {
	    String query = "SELECT point_production FROM user WHERE login = ?";
	    try (Connection conn = this.init();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, login);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("point_production") >= 15;
	        }
	    }
	    return false;
	}

	//level pour le joueur
	public void updatePlayerLevel(String userLogin,String code) throws SQLException {
		
	    int villeCount = compterVillesPossedeesParUtilisateur(userLogin);
	    int soldierCount = compterSoldatsPossedesParUtilisateur(userLogin,code);  // Assurez-vous d'avoir cette m�thode dans SoldatBDD

	    System.out.println("Utilisateur " + userLogin + " poss�de " + villeCount + " ville(s) et " + soldierCount + " soldat(s).");
	    
	    int newLevel = 0; // Niveau par d�faut
	    if ((villeCount >= 1 && villeCount < 2) || (soldierCount >= 2 && soldierCount < 4)) {
	        newLevel = 1;
	    } else if ((villeCount >= 2) || (soldierCount >= 4)) {
	        newLevel = 2;
	    }
	    // Ajoutez d'autres niveaux selon vos crit�res

	    String sql = "UPDATE user SET level = ? WHERE login = ?";
	    try (Connection conn = this.init();  // Assurez-vous d'avoir la bonne m�thode de connexion
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, newLevel);
	        pstmt.setString(2, userLogin);
	        pstmt.executeUpdate();
	    }
	}


    // Récupérer les détails complets d'un utilisateur
	public User getUserDetails(String login) throws SQLException {
	    String query = "SELECT login, point_production, soldierImage FROM user WHERE login = ?";
	    try (Connection conn = this.init();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, login);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            // Crée et retourne un objet User avec les détails récupérés
	            String soldierImage = rs.getString("soldierImage");
	            User user = new User(0, rs.getString("login"), null, rs.getInt("point_production"), soldierImage);
	            return user;
	        }
	    }
	    return null; // Retourne null si aucun utilisateur n'est trouvé
	}

	
 // Méthode pour compter les villes possédées par un utilisateur spécifique
    public int compterVillesPossedeesParUtilisateur(String userLogin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ville WHERE id_user = ?";
        try (Connection conn = this.init();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userLogin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;  // Retourne 0 si aucun résultat n'est trouvé
            }
        }
    }
    
    public int compterSoldatsPossedesParUtilisateur(String userLogin,String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM soldat WHERE (login_user = ?) AND (code = ?)";
        try (Connection conn = this.init();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userLogin);
			pstmt.setString(2, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;  // Retourne 0 si aucun résultat n'est trouvé
            }
        }
    }
        
    public String getSoldierImage(String login) throws SQLException {
        String query = "SELECT soldierImage FROM user WHERE login = ?";
        try (Connection conn = this.init();
        	PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("soldierImage");
            }
        }
        return "default-soldier.png"; // Valeur par défaut
    }
    public int getUserScore(String login) throws SQLException {
        String query = "SELECT score FROM user WHERE login = ?";
        try (Connection conn = this.init();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("score");
            }
        }
        return 0;
    }

    public void updateScore(String login, int additionalScore) throws SQLException {
        String sql = "UPDATE user SET score = score + ? WHERE login = ?";
        try (Connection cnx = this.init();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, additionalScore);
            stmt.setString(2, login);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("No user found with login: " + login);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getTopPlayer() throws SQLException {
        String query = "SELECT login FROM user ORDER BY point_production DESC LIMIT 1";
        try (Connection conn = this.init();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("login"); // Assurez-vous que le nom de la colonne correspond
            }
        }
        return null; // Aucun joueur trouvé
    }

	public List<String> getUsedSoldierImages() throws SQLException {
		List<String> usedImages = new ArrayList<>();
		String sql = "SELECT DISTINCT soldierImage FROM user WHERE soldierImage IS NOT NULL";
		try (Connection conn = init();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery())
		{
			while (rs.next())
			{
				usedImages.add(rs.getString("soldierImage"));
			}
		}
		return usedImages;
	}





}