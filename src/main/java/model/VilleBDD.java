package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    // MÃ¯Â¿Â½thode pour initialiser les villes Ã¯Â¿Â½ partir d'un fichier CSV
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

    // Méthode pour ajouter une ville Ã¯Â¿Â½ la base de donnÃ¯Â¿Â½es
    private void ajouterVilleBDD(int x, int y, int defensePoints, String login_user) throws SQLException {
        // VÃ¯Â¿Â½rifie si la ville existe dÃ¯Â¿Â½jÃ¯Â¿Â½ pour Ã¯Â¿Â½viter les doublons
        if (villeExiste(x, y)) {
            System.out.println("Ville déjà existante à la position (" + x + ", " + y + ")");
            return; // Ville dÃ¯Â¿Â½jÃ¯Â¿Â½ existante, donc on ne fait rien
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
                return rs.next();  // Retourne vrai si une ligne est trouvÃ¯Â¿Â½e, donc la ville existe dÃ¯Â¿Â½jÃ¯Â¿Â½
            }
        }
    }
    // 
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
        return null;  // Retourne null si la ville n'a pas de propriÃ¯Â¿Â½taire
    }

    // MÃ¯Â¿Â½thode pour mettre Ã¯Â¿Â½ jour les points de production d'un utilisateur
    public void mettreAJourPointsDeProduction(String userLogin, int pointsToAdd) throws SQLException {
        String sql = "UPDATE users SET production_points = production_points + ? WHERE login = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pointsToAdd);
            pstmt.setString(2, userLogin);
            pstmt.executeUpdate();
        }
    }
    
    public int getCityDefensePoints(int x, int y) throws SQLException {
        String sql = "SELECT point_defense FROM ville WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int points = rs.getInt("point_defense");
                    System.out.println("Points de défense récupérés pour la ville (" + x + ", " + y + ") : " + points);
                    return points;
                }
            }
        }
        System.out.println("Aucune ville trouvée à la position (" + x + ", " + y + ").");
        return 0; // Retourne 0 si aucune ville n'existe à cette position
    }



    public void updateCityOwner(int x, int y, String newOwnerLogin) throws SQLException {
        String sql = "UPDATE ville SET id_user = ? WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newOwnerLogin);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Le propriétaire de la ville a été mis à jour avec succès.");
            } else {
                System.out.println("Aucune ville n'a été mise à jour.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du propriétaire de la ville : " + e.getMessage());
            throw e;
        }
    }


    public void updateDefensePoints(int x, int y, int newDefensePoints) throws SQLException {
        String query = "UPDATE ville SET point_defense = ? WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newDefensePoints);
            stmt.setInt(2, x);
            stmt.setInt(3, y);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Mise à jour réussie pour la ville (" + x + ", " + y + ") avec " + newDefensePoints + " points de défense.");
            } else {
                System.out.println("Aucune ville n'a été mise à jour pour (" + x + ", " + y + ").");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour des points de défense pour (" + x + ", " + y + ") : " + e.getMessage());
            throw e;
        }
    }

    
    public void updateDefensePointsAndOwner(int x, int y, int newDefensePoints, String attackerLogin) throws SQLException {
        String query = "UPDATE ville SET point_defense = ?, id_user = CASE WHEN ? = 0 THEN ? ELSE id_user END WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newDefensePoints);
            stmt.setInt(2, newDefensePoints);
            stmt.setString(3, attackerLogin);
            stmt.setInt(4, x);
            stmt.setInt(5, y);
            stmt.executeUpdate();
        }
    }
    public Ville getCityByPosition(int x, int y) throws SQLException {
        String sql = "SELECT id_ville, x_position, y_position, point_defense, id_user FROM ville WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Ville ville = new Ville();
                    ville.setId(rs.getString("id_ville"));
                    ville.setPositionX(rs.getInt("x_position"));
                    ville.setPositionY(rs.getInt("y_position"));
                    ville.setDefensePoints(rs.getInt("point_defense"));
                    ville.setOwner(rs.getString("id_user"));
                    return ville;
                }
            }
        }
        return null; // Retourne null si aucune ville n'est trouvÃƒÂ©e
    }


    public List<int[]> getAllCitiesPositions(String userLogin) throws SQLException {
        String query = "SELECT x_position, y_position FROM ville WHERE id_user = ?";
        List<int[]> positions = new ArrayList<>();
        try (Connection conn = initConnection1();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userLogin);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x_position");
                    int y = rs.getInt("y_position");
                    positions.add(new int[]{x, y});
                }
            }
        }
        return positions;
    }


}


