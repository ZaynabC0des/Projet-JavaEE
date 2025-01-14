package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForetBDD {

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

    public void initializeTree(String csvFilePath) throws IOException, SQLException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int col = 0; col < values.length; col++) {
                    if ("2".equals(values[col].trim())) {
                        ajouterForetBDD(row, col);
                    }
                }
                row++;
            }
        }
    }

    private void ajouterForetBDD(int x, int y) throws SQLException {
        if (!ForetExiste(x, y)) {
            String sql = "INSERT INTO foret (x_position, y_position) VALUES (?, ?)";
            try (Connection conn = initConnection1();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, x);
                pstmt.setInt(2, y);
                pstmt.executeUpdate();
            }
        } else {
            System.out.println("La foret est déjà existante à la position (" + x + ", " + y + ")");
        }
    }

    private boolean ForetExiste(int x, int y) throws SQLException {
        String sql = "SELECT 1 FROM foret WHERE x_position = ? AND y_position = ?";
        try (Connection conn = initConnection1();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  // Retourne vrai si une ligne est trouvée
            }
        }
    }
}
