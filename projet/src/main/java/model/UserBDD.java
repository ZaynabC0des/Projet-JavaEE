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
	    Connection cnx = this.init();
	    Statement stm = cnx.createStatement();
	    String sql = "INSERT INTO user ( login, password) VALUES ('" + u.getLogin() + "', '" + u.getPassword() + "')";
	    int result = stm.executeUpdate(sql);
	    return result > 0;
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

	
	
	

}
