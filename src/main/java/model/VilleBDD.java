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

    // Mï¿½thode pour initialiser les villes ï¿½ partir d'un fichier CSV
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

    // Mï¿½thode pour ajouter une ville ï¿½ la base de donnï¿½es
    private void ajouterVilleBDD(int x, int y, int defensePoints, String login_user) throws SQLException {
        // Vï¿½rifie si la ville existe dï¿½jï¿½ pour ï¿½viter les doublons
        if (villeExiste(x, y)) {
            System.out.println("Ville dï¿½jï¿½ existante ï¿½ la position (" + x + ", " + y + ")");
            return; // Ville dï¿½jï¿½ existante, donc on ne fait rien
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
                return rs.next();  // Retourne vrai si une ligne est trouvï¿½e, donc la ville existe dï¿½jï¿½
            }
        }
    }
    // Mï¿½thode pour rï¿½cupï¿½rer le propriï¿½taire de la ville ï¿½ une position spï¿½cifique
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
        return null;  // Retourne null si la ville n'a pas de propriï¿½taire
    }

    // Mï¿½thode pour mettre ï¿½ jour les points de production d'un utilisateur
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
                    return rs.getInt("point_defense");
                }
            }
        }
        return 0; // Retourne 0 si aucun point de dï¿½fense n'est trouvï¿½
    }


    public void updateCityOwner(int x, int y, String newOwnerLogin) throws SQLException {
        String sql = "UPDATE ville SET id_user = ? WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newOwnerLogin);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Le propriÃ©taire de la ville a Ã©tÃ© mis Ã  jour avec succÃ¨s.");
            } else {
                System.out.println("Aucune ville n'a Ã©tÃ© mise Ã  jour.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise Ã  jour du propriÃ©taire de la ville : " + e.getMessage());
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
            stmt.executeUpdate();
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
        return null; // Retourne null si aucune ville n'est trouvÃ©e
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


