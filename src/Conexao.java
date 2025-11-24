import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    // Configurações para o driver Xerial SQLite JDBC
    private static final String DRIVER = "org.sqlite.JDBC";

    // URL: O banco 'aulajava.db' será criado na raiz do projeto
    private static final String URL = "jdbc:sqlite:aulajava.db";

    /**
     * Tenta estabelecer e retorna uma nova conexão.
     */
    public static Connection getConexao() {
        try {
            // 1. Carrega o Driver
            Class.forName(DRIVER);

            // 2. Estabelece a Conexão (SQLite não precisa de usuário/senha)
            return DriverManager.getConnection(URL);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Erro: Driver JDBC SQLite não encontrado. Verifique o .jar no Classpath.");
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro: Falha na conexão com o banco de dados SQLite. " + e.getMessage());
            return null;
        }
    }

    /**
     * Fecha o objeto Connection.
     */
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