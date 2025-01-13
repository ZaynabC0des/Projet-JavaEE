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
    public int ajouterSoldatEtRecupererId(String login, int pointDeVie, int x, int y,String code) {
        String sql = "INSERT INTO soldat (login_user, point_de_vie, x_position , y_position,code) VALUES (?, ?, ?, ?,?)";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, login);
            stmt.setInt(2, pointDeVie);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setString(5, code);

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

    public boolean existeSoldatPosition(int x, int y,String code) {
        String sql = "SELECT 1 FROM soldat WHERE x_position = ? AND y_position = ? AND code= ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setString(3, code);

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

    public boolean updatePointsDeVie(int idSoldat, int newPoints) {
        String sql = "UPDATE soldat SET point_de_vie = ? WHERE id_soldat = ?";
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
    
    public List<Soldat> getSoldatsByUser(String userLogin) throws SQLException {
        String query = "SELECT s.id_soldat, s.x_position, s.y_position, s.point_de_vie, u.soldierImage " +
                       "FROM soldat s " +
                       "JOIN user u ON s.login_user = u.login " +
                       "WHERE s.login_user = ?";
        List<Soldat> soldats = new ArrayList<>();
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userLogin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Soldat soldat = new Soldat();
                soldat.setId(rs.getInt("id_soldat"));
                soldat.setX(rs.getInt("x_position"));
                soldat.setY(rs.getInt("y_position"));
                soldat.setPointDeVie(rs.getInt("point_de_vie"));

                // R�cup�rer le chemin de l'image
                String imagePath = rs.getString("soldierImage");
                soldat.setImagePath(imagePath);
               
                
             // Log du chemin de l'image
                System.out.println("Soldat ID: " + soldat.getId() + ", Image Path: " + imagePath);

                soldats.add(soldat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return soldats;
    }
    
    public List<Soldat> getAllSoldats(String code) throws SQLException {
        String query = "SELECT s.id_soldat, s.x_position, s.y_position, s.point_de_vie, u.soldierImage " +
                       "FROM soldat s " +
                       "JOIN user u ON s.login_user = u.login WHERE s.code='" + code+"'";
        List<Soldat> soldats = new ArrayList<>();
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Soldat soldat = new Soldat();
                soldat.setId(rs.getInt("id_soldat"));
                soldat.setX(rs.getInt("x_position"));
                soldat.setY(rs.getInt("y_position"));
                soldat.setPointDeVie(rs.getInt("point_de_vie"));

                // R�cup�rer le chemin de l'image
                String imagePath = rs.getString("soldierImage");
                soldat.setImagePath(imagePath);

                // Log du chemin de l'image
             //   System.out.println("Soldat ID: " + soldat.getId() + ", Image Path: " + imagePath);

                soldats.add(soldat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return soldats;
    }



    public Soldat getSoldatById(int soldatId) throws SQLException {
        String query = "SELECT id_soldat, x_position, y_position, point_de_vie, login_user FROM soldat WHERE id_soldat = ?";
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, soldatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Soldat soldat = new Soldat();
                soldat.setId(rs.getInt("id_soldat"));
                soldat.setX(rs.getInt("x_position"));
                soldat.setY(rs.getInt("y_position"));
                soldat.setPointDeVie(rs.getInt("point_de_vie"));
                soldat.setOwner(rs.getString("login_user")); // Propri�taire du soldat
                return soldat;
            }
        }
        return null;
    }

    public boolean updatePosition(int soldatId, int newX, int newY) {
        String sql = "UPDATE soldat SET x_position = ?, y_position = ? WHERE id_soldat = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {

            stmt.setInt(1, newX);
            stmt.setInt(2, newY);
            stmt.setInt(3, soldatId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int healSoldiers(String userLogin, String gameCode) throws SQLException {
        String query = "UPDATE soldat " +
                       "SET point_de_vie = LEAST(point_de_vie + 15, 100) " +
                       "WHERE point_de_vie < 50 AND login_user = ? AND code = ?";
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userLogin);
            stmt.setString(2, gameCode);

            return stmt.executeUpdate(); // Retourne le nombre de lignes mises � jour
        }
    }

    public Soldat getEnemySoldatAtPosition(int x, int y, String code, String currentPlayer) throws SQLException {
        String query = "SELECT id_soldat, x_position, y_position, point_de_vie, login_user " +
                       "FROM soldat " +
                       "WHERE x_position = ? AND y_position = ? AND code = ? AND login_user != ?";
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setString(3, code);
            stmt.setString(4, currentPlayer);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Soldat soldat = new Soldat();
                soldat.setId(rs.getInt("id_soldat"));
                soldat.setX(rs.getInt("x_position"));
                soldat.setY(rs.getInt("y_position"));
                soldat.setPointDeVie(rs.getInt("point_de_vie"));
                soldat.setOwner(rs.getString("login_user"));
                return soldat;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Aucun soldat ennemi trouv�
    }

    public boolean updateSoldat(Soldat soldat) {
        String sql = "UPDATE soldat SET point_de_vie = ?, x_position = ?, y_position = ? WHERE id_soldat = ?";
        try (Connection cnx = initConnection();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, soldat.getPointDeVie());
            stmt.setInt(2, soldat.getX());
            stmt.setInt(3, soldat.getY());
            stmt.setInt(4, soldat.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean removeSoldat(int idSoldat) {
        String sql = "DELETE FROM soldat WHERE id_soldat = ?";
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

    public Soldat getSoldatAtPosition(int x, int y) throws SQLException {
        String query = "SELECT id_soldat, x_position, y_position, point_de_vie, login_user " +
                       "FROM soldat WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Soldat soldat = new Soldat();
                soldat.setId(rs.getInt("id_soldat"));
                soldat.setX(rs.getInt("x_position"));
                soldat.setY(rs.getInt("y_position"));
                soldat.setPointDeVie(rs.getInt("point_de_vie"));
                soldat.setOwner(rs.getString("login_user"));
                return soldat;
            }
        }
        return null;
    }

    public String getLoginBySoldatId(int soldatId) throws SQLException {
        String query = "SELECT login_user FROM soldat WHERE id_soldat = ?";
        try (Connection conn = initConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, soldatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("login_user"); // Retourne le login du joueur
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si aucun joueur n'est trouv�
    }



  
    

    
    
}
