import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TelaConsulta extends JFrame implements ActionListener {

    private static class Funcionario {
        String nome;
        double salario;
        String cargo;

        public Funcionario(String nome, double salario, String cargo) {
            this.nome = nome;
            this.salario = salario;
            this.cargo = cargo;
        }
    }

    private JTextField txtPesquisa, txtNome, txtSalario, txtCargo;
    private JButton btnPesquisar, btnAnterior, btnProximo;

    private Connection con;
    private PreparedStatement pst;

    private List<Funcionario> listaResultados = new ArrayList<>();
    private int indiceAtual = -1; 

    private DecimalFormat moneyFormat = new DecimalFormat("0.00");

    public TelaConsulta() {
        super("TRABALHO PRATICO 04");
        montarInterface();
        abrirConexao();
        if (con != null) {
            inicializarBanco();
        }
        configurarFrame();
    }


    private void montarInterface() {
        setLayout(null);

        JLabel lblNomePesquisa = new JLabel("Nome:");
        lblNomePesquisa.setBounds(10, 20, 50, 25);
        add(lblNomePesquisa);

        txtPesquisa = new JTextField(15);
        txtPesquisa.setBounds(60, 20, 200, 25);
        add(txtPesquisa);

        btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.setBounds(270, 20, 100, 25);
        btnPesquisar.addActionListener(this);
        add(btnPesquisar);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setBounds(5, 60, 385, 5);
        add(separator);

        int y_start = 75;

        addLabelAndField(this, "Nome:", 10, y_start, 60, txtNome = new JTextField(20));
        addLabelAndField(this, "Salário:", 10, y_start + 40, 60, txtSalario = new JTextField(10));
        addLabelAndField(this, "Cargo:", 10, y_start + 80, 60, txtCargo = new JTextField(15));

        txtNome.setEditable(false);
        txtSalario.setEditable(false);
        txtCargo.setEditable(false);

        btnAnterior = new JButton("Anterior");
        btnAnterior.setBounds(100, y_start + 140, 90, 30);
        btnAnterior.addActionListener(this);
        btnAnterior.setEnabled(false);
        add(btnAnterior);

        btnProximo = new JButton("Próximo");
        btnProximo.setBounds(200, y_start + 140, 90, 30);
        btnProximo.addActionListener(this);
        btnProximo.setEnabled(false);
        add(btnProximo);
    }

    private void addLabelAndField(JFrame frame, String labelText, int x, int y, int labelWidth, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setBounds(x, y, labelWidth, 25);
        frame.add(label);

        field.setBounds(x + labelWidth, y, 250, 25);
        frame.add(field);
    }

    private void configurarFrame() {
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void abrirConexao() {
        con = Conexao.getConexao();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Falha ao conectar ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            btnPesquisar.setEnabled(false);
        }
    }

    private void inicializarBanco() {
        String sqlCreateCargos = "CREATE TABLE IF NOT EXISTS tbcargos ("
                + "cd_cargo INTEGER PRIMARY KEY,"
                + "ds_cargo TEXT NOT NULL"
                + ");";

        String sqlCreateFuncs = "CREATE TABLE IF NOT EXISTS tbfuncs ("
                + "cod_func INTEGER PRIMARY KEY,"
                + "nome_func TEXT NOT NULL,"
                + "sal_func REAL,"
                + "cod_cargo INTEGER,"
                + "FOREIGN KEY (cod_cargo) REFERENCES tbcargos(cd_cargo)"
                + ");";

        String sqlCheckCargos = "SELECT COUNT(*) FROM tbcargos";

        try (Statement stmt = con.createStatement()) {

            stmt.execute(sqlCreateCargos);
            stmt.execute(sqlCreateFuncs);

            ResultSet rs = stmt.executeQuery(sqlCheckCargos);
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inserindo dados iniciais...");
                stmt.executeUpdate("INSERT INTO tbcargos (cd_cargo, ds_cargo) VALUES (10, 'Administrativo')");
                stmt.executeUpdate("INSERT INTO tbcargos (cd_cargo, ds_cargo) VALUES (20, 'Vendas')");
                stmt.executeUpdate("INSERT INTO tbcargos (cd_cargo, ds_cargo) VALUES (30, 'Financeiro')");

                stmt.executeUpdate("INSERT INTO tbfuncs (cod_func, nome_func, sal_func, cod_cargo) VALUES (1001, 'Marcelo Silva', 2000.00, 10)");
                stmt.executeUpdate("INSERT INTO tbfuncs (cod_func, nome_func, sal_func, cod_cargo) VALUES (1002, 'Ana Paula', 3500.50, 20)");
                stmt.executeUpdate("INSERT INTO tbfuncs (cod_func, nome_func, sal_func, cod_cargo) VALUES (1003, 'Carlos Oliveira', 1800.00, 10)");
                stmt.executeUpdate("INSERT INTO tbfuncs (cod_func, nome_func, sal_func, cod_cargo) VALUES (1004, 'Beatriz Souza', 4200.00, 30)");
            }
            rs.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao inicializar banco: " + e.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            btnPesquisar.setEnabled(false);
        }
    }


    private void limparCampos() {
        txtNome.setText("");
        txtSalario.setText("");
        txtCargo.setText("");
    }

    private void exibirDados() {
        if (indiceAtual >= 0 && indiceAtual < listaResultados.size()) {
            Funcionario func = listaResultados.get(indiceAtual);
            txtNome.setText(func.nome);
            txtSalario.setText(moneyFormat.format(func.salario));
            txtCargo.setText(func.cargo);
        } else {
            limparCampos();
        }

        atualizarBotoes();
    }

    private void atualizarBotoes() {
        boolean temResultados = !listaResultados.isEmpty();

        btnAnterior.setEnabled(temResultados && indiceAtual > 0);

        btnProximo.setEnabled(temResultados && indiceAtual < listaResultados.size() - 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnPesquisar) {
            pesquisar();
        } else if (e.getSource() == btnProximo) {
            navegarProximo();
        } else if (e.getSource() == btnAnterior) {
            navegarAnterior();
        }
    }

    private void pesquisar() {
        limparCampos();
        listaResultados = new ArrayList<>(); 
        indiceAtual = -1;

        String sql = "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ? " +
                "ORDER BY f.nome_func";

        try {
            if (pst != null) pst.close();

            pst = con.prepareStatement(sql);

            String termo = "%" + txtPesquisa.getText().trim() + "%";
            pst.setString(1, termo);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String nome = rs.getString("nome_func");
                double salario = rs.getDouble("sal_func");
                String cargo = rs.getString("ds_cargo");
                listaResultados.add(new Funcionario(nome, salario, cargo));
            }
            rs.close(); 

            if (!listaResultados.isEmpty()) {
                indiceAtual = 0;
                exibirDados();
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum registro encontrado para '" + txtPesquisa.getText() + "'.");
                limparCampos();
                atualizarBotoes();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao executar pesquisa: " + ex.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            limparCampos();
            atualizarBotoes();
        }
    }

    private void navegarProximo() {
        if (indiceAtual < listaResultados.size() - 1) {
            indiceAtual++;
            exibirDados();


    private void navegarAnterior() {
        if (indiceAtual > 0) {
            indiceAtual--;
            exibirDados();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaConsulta());
    }

    @Override
    public void dispose() {
        super.dispose();
        Conexao.fecharConexao(con);
        try {
            if (pst != null) pst.close();
        } catch (SQLException e) {
        }
    }

}
