package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SoldatBDD {

    private Connection initConnection() {
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

    // Ajouter un soldat
    public int ajouterSoldatEtRecupererId(String login, int pointDeVie, int x, int y) {
        String sql = "INSERT INTO soldat (login_user, point_de_vie, x_position , y_position) VALUES (?, ?, ?, ?)";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, login);
            stmt.setInt(2, pointDeVie);
            stmt.setInt(3, x);
            stmt.setInt(4, y);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retourne l'ID g�n�r� pour id_soldat
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'�chec
    }

    public boolean existeSoldatPosition(int x, int y) {
        String sql = "SELECT 1 FROM soldat WHERE x_position = ? AND y_position = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, x);
            stmt.setInt(2, y);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Retourne vrai si une ligne est trouv�e
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // En cas d'erreur, consid�rer qu'il n'y a pas de soldat
    }

    // Obtenir tous les soldats d'un joueur
    public ResultSet getSoldatsByJoueur(int idJoueur) {
        String sql = "SELECT * FROM soldat WHERE id_user = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, idJoueur);
            return stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Mettre � jour les points de vie d'un soldat
    public boolean updatePointsDeVie(int idSoldat, int newPoints) {
        String sql = "UPDATE soldat SET point_de_vie = ? WHERE id = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, newPoints);
            stmt.setInt(2, idSoldat);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Supprimer un soldat
    public boolean supprimerSoldat(int idSoldat) {
        String sql = "DELETE FROM soldat WHERE id = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, idSoldat);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Soldat> getSoldatsByUser(String loginUser) {
        String sql = "SELECT id_soldat, x_position, y_position, point_de_vie FROM soldat WHERE login_user = ?";
        List<Soldat> soldats = new ArrayList<>();
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setString(1, loginUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_soldat");
                int x = rs.getInt("x_position");
                int y = rs.getInt("y_position");
                int pointsDeVie = rs.getInt("point_de_vie");
                Soldat soldat = new Soldat(x, y, loginUser, pointsDeVie);
                soldat.setId(id);
                soldats.add(soldat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soldats;
    }

    
  
    

    
    
}
