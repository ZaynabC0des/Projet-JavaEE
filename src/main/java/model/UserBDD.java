package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserBDD {
	
	public Connection init() throws ClassNotFoundException {
	    String url = "jdbc:mysql://localhost:3306/projet_jee";
	    String user = "root";
	    String password = "";
	    Connection cnx = null;
	    Class.forName("com.mysql.jdbc.Driver");
	    try {
	        cnx = DriverManager.getConnection(url, user, password);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return cnx;
	}
	
	//ajout d un utilisateur dans la bdd
	public boolean addUser(User u) throws SQLException, ClassNotFoundException {
	    String sql = "INSERT INTO user (login, password) VALUES (?, ?)";
	    try (Connection cnx = this.init();
	         PreparedStatement stmt = cnx.prepareStatement(sql)) {
	        stmt.setString(1, u.getLogin());
	        stmt.setString(2, u.getPassword());
	        int result = stmt.executeUpdate();
	        return result > 0;
	    } catch (SQLException | ClassNotFoundException e) {
	        e.printStackTrace();
	        throw e; // Relance l'exception pour la g�rer plus haut dans la stack
	    }
	}

	//recherche de l utilisateur dans la bdd
	public User findUser(User u) throws SQLException, ClassNotFoundException {
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
	public boolean checkUserExists(String login) throws SQLException, ClassNotFoundException {
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
	    String sql = "UPDATE user SET point_production = point_production + ? WHERE login = ?";
	    try (Connection cnx = this.init();
	         PreparedStatement stmt = cnx.prepareStatement(sql)) {
	        stmt.setInt(1, pointChange);
	        stmt.setString(2, login);
	        int result = stmt.executeUpdate();
	        return result > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw e;
	    } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

	
	public int getProductionPoints(String login) throws SQLException, ClassNotFoundException {
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
	    } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
	}

	//level pour le joueur
	public void updatePlayerLevel(String userLogin) throws SQLException, ClassNotFoundException {
	    VilleBDD villeBDD = new VilleBDD();
	    SoldatBDD soldatBDD = new SoldatBDD();
	    
	    int villeCount = villeBDD.compterVillesPossedeesParUtilisateur(userLogin);
	    int soldierCount = soldatBDD.compterSoldatsPossedesParUtilisateur(userLogin);  // Assurez-vous d'avoir cette m�thode dans SoldatBDD

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
        String query = "SELECT login, point_production FROM user WHERE login = ?";
        try (Connection conn = this.init();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Crée et retourne un objet User avec les détails récupérés
                User user = new User(0, query, query, 0);
                user.setLogin(rs.getString("login"));
                user.setPointProduction(rs.getInt("point_production"));
                return user;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null; // Retourne null si aucun utilisateur n'est trouvé
    }
	
	
	

}
