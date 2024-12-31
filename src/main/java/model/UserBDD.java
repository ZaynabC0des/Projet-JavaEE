package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserBDD {
	
	public Connection init() {
	    String url = "jdbc:mysql://localhost:3306/projet_jee";
	    String user = "root";
	    String password = "";
	    Connection cnx = null;
	    
	    try {
	        cnx = DriverManager.getConnection(url, user, password);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return cnx;
	}
	
	//ajout d un utilisateur dans la bdd
	public boolean addUser(User u) throws SQLException {
	    String sql = "INSERT INTO user (login, password) VALUES (?, ?)";
	    try (Connection cnx = this.init();
	         PreparedStatement stmt = cnx.prepareStatement(sql)) {
	        stmt.setString(1, u.getLogin());
	        stmt.setString(2, u.getPassword());
	        int result = stmt.executeUpdate();
	        return result > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw e; // Relance l'exception pour la gérer plus haut dans la stack
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
	    return 0; // Valeur par défaut si aucun utilisateur trouvé
	
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
	


	
	
	

}
