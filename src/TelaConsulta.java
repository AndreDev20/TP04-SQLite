import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TelaConsulta extends JFrame implements ActionListener {

    // --- 1. CLASSE INTERNA PARA ARMAZENAMENTO DE DADOS (Funcionario) ---
    // Usada para armazenar os resultados da consulta em memória (solução para o SQLite)
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

    // --- 2. VARIÁVEIS DE ESTADO E COMPONENTES ---
    private JTextField txtPesquisa, txtNome, txtSalario, txtCargo;
    private JButton btnPesquisar, btnAnterior, btnProximo;

    private Connection con;
    private PreparedStatement pst;

    // NOVOS OBJETOS PARA NAVEGAÇÃO EM MEMÓRIA
    private List<Funcionario> listaResultados = new ArrayList<>();
    private int indiceAtual = -1; // Índice atual na lista

    private DecimalFormat moneyFormat = new DecimalFormat("0.00");

    public TelaConsulta() {
        super("TRABALHO PRATICO 04");
        montarInterface();
        abrirConexao();
        // Inicializa a estrutura do banco após a conexão ser estabelecida
        if (con != null) {
            inicializarBanco();
        }
        configurarFrame();
    }

    // --- 3. MÉTODOS DE CONFIGURAÇÃO E INICIALIZAÇÃO ---

    private void montarInterface() {
        setLayout(null);

        // Painel Superior (Pesquisa)
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

        // Separador visual
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setBounds(5, 60, 385, 5);
        add(separator);

        // Painel de Dados
        int y_start = 75;

        addLabelAndField(this, "Nome:", 10, y_start, 60, txtNome = new JTextField(20));
        addLabelAndField(this, "Salário:", 10, y_start + 40, 60, txtSalario = new JTextField(10));
        addLabelAndField(this, "Cargo:", 10, y_start + 80, 60, txtCargo = new JTextField(15));

        txtNome.setEditable(false);
        txtSalario.setEditable(false);
        txtCargo.setEditable(false);

        // Painel de Navegação
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
        // Usa a classe Conexao.java (externa)
        con = Conexao.getConexao();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Falha ao conectar ao banco de dados.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            btnPesquisar.setEnabled(false);
        }
    }

    // Método que cria as tabelas e insere dados iniciais no SQLite, se necessário.
    private void inicializarBanco() {
        // SQLs para criação de tabelas
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

        // SQL para verificar se a tabela de cargos está vazia
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

    // --- 4. LÓGICA DE EXIBIÇÃO E NAVEGAÇÃO (Em Memória) ---

    private void exibirDados() {
        // Exibe o registro da lista correspondente ao índice atual
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

        // Habilita Anterior se não for o primeiro (índice > 0)
        btnAnterior.setEnabled(temResultados && indiceAtual > 0);

        // Habilita Próximo se não for o último (índice menor que o tamanho - 1)
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

    // Implementa a lógica do botão Pesquisar (SELECT + LIKE)
    private void pesquisar() {
        limparCampos();
        listaResultados = new ArrayList<>(); // Reseta a lista
        indiceAtual = -1; // Reseta o índice

        // SQL para SELECT (JOIN) e busca (LIKE)
        String sql = "SELECT f.nome_func, f.sal_func, c.ds_cargo " +
                "FROM tbfuncs f JOIN tbcargos c ON f.cod_cargo = c.cd_cargo " +
                "WHERE f.nome_func LIKE ? " +
                "ORDER BY f.nome_func";

        try {
            if (pst != null) pst.close();

            // Usa a versão padrão, que é TYPE_FORWARD_ONLY (correto para SQLite)
            pst = con.prepareStatement(sql);

            String termo = "%" + txtPesquisa.getText().trim() + "%";
            pst.setString(1, termo);

            ResultSet rs = pst.executeQuery();

            // ** Lê TODOS os resultados do ResultSet e armazena na lista Funcionario **
            while (rs.next()) {
                String nome = rs.getString("nome_func");
                double salario = rs.getDouble("sal_func");
                String cargo = rs.getString("ds_cargo");
                listaResultados.add(new Funcionario(nome, salario, cargo));
            }
            rs.close(); // Fecha o ResultSet

            // Posiciona no primeiro registro e exibe
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

    // Implementa a lógica do botão Próximo (avançando no índice da lista)
    private void navegarProximo() {
        if (indiceAtual < listaResultados.size() - 1) {
            indiceAtual++;
            exibirDados();



    // Implementa a lógica do botão Anterior (retrocedendo no índice da lista)
    private void navegarAnterior() {
        if (indiceAtual > 0) {
            indiceAtual--;
            exibirDados();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaConsulta());
    }

    // Garante que a conexão seja fechada ao sair do frame
    @Override
    public void dispose() {
        super.dispose();
        Conexao.fecharConexao(con);
        try {
            if (pst != null) pst.close();
        } catch (SQLException e) {
            // Ignora erros ao fechar
        }
    }
}