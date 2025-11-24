import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String DRIVER = "org.sqlite.JDBC";

    private static final String URL = "jdbc:sqlite:aulajava.db";

    
    public static Connection getConexao() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL);

        } 
        } catch (SQLException e) {
            System.err.println("❌ Erro: Falha na conexão com o banco de dados SQLite. " + e.getMessage());
            return null;
        }
    }

    public static void fecharConexao(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

}
