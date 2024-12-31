package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public int ajouterSoldatEtRecupererId(int id_user, int pointDeVie) {
        String sql = "INSERT INTO soldat (id_user, point_de_vie) VALUES (?, ?)";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, id_user); // ID de l'utilisateur
            stmt.setInt(2, pointDeVie); // Points de vie

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retourne l'ID généré pour id_soldat
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'échec
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

    // Mettre à jour les points de vie d'un soldat
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
}
