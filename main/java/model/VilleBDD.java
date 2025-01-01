package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VilleBDD {

	private Connection initConnection1() {
        String url = "jdbc:mysql://localhost:3306/projet_jee";
        String user = "root";
        String password = "";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
	
    // M�thode pour initialiser les villes � partir d'un fichier CSV
    public void initializeCities(String csvFilePath) throws IOException, SQLException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int col = 0; col < values.length; col++) {
                    if ("1".equals(values[col].trim())) {
                        ajouterVilleBDD(row, col, 100, null);
                    }
                }
                row++;
            }
        }
    }

    // M�thode pour ajouter une ville � la base de donn�es
    private void ajouterVilleBDD(int x, int y, int defensePoints, String login_user) throws SQLException {
    	 // V�rifie si la ville existe d�j� pour �viter les doublons
        if (villeExiste(x, y)) {
            System.out.println("Ville d�j� existante � la position (" + x + ", " + y + ")");
            return; // Ville d�j� existante, donc on ne fait rien
        }
        
        String sql = "INSERT INTO ville (x_position, y_position, point_defense, id_user) VALUES (?, ?, ?, ?)";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, defensePoints);
            if (login_user == null) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, login_user);
            }
            pstmt.executeUpdate();
        }
    }


private boolean villeExiste(int x, int y) throws SQLException {
    String sql = "SELECT 1 FROM ville WHERE x_position = ? AND y_position = ?";
    try (Connection conn = initConnection1();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, x);
        pstmt.setInt(2, y);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();  // Retourne vrai si une ligne est trouv�e, donc la ville existe d�j�
        }
    }
}
    // M�thode pour r�cup�rer le propri�taire de la ville � une position sp�cifique
    public String getCityOwner(int x, int y) throws SQLException {
        String sql = "SELECT id_user FROM ville WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id_user");
                }
            }
        }
        return null;  // Retourne null si la ville n'a pas de propri�taire
    }
    
    // M�thode pour mettre � jour les points de production d'un utilisateur
    public void mettreAJourPointsDeProduction(String userLogin, int pointsToAdd) throws SQLException {
        String sql = "UPDATE users SET production_points = production_points + ? WHERE login = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pointsToAdd);
            pstmt.setString(2, userLogin);
            pstmt.executeUpdate();
        }
    }
    
    // M�thode pour compter les villes poss�d�es par un utilisateur sp�cifique
    public int compterVillesPossedeesParUtilisateur(String userLogin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ville WHERE id_user = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userLogin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;  // Retourne 0 si aucun r�sultat n'est trouv�
            }
        }
    }
    
    

}
