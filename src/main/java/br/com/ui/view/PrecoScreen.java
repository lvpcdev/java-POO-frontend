package br.com.ui.view;

import br.com.model.Preco;
import br.com.model.Produto;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PrecoScreen extends JFrame {

    private JComboBox<String> produtoComboBox;
    private JTextField valorField, dataAlteracaoField;
    private JTable tabelaPrecos;
    private DefaultTableModel tableModel;
    private Map<String, Long> produtoMap;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private List<Preco> precos = new ArrayList<>();
    private long nextPrecoId = 1;

    public PrecoScreen() {
        setTitle("Gerenciamento de Preços por Produto");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cores
        Color background = new Color(240, 240, 240);
        Color primary = new Color(163, 31, 52);
        Color secondary = new Color(0, 153, 102);
        Color text = new Color(51, 51, 51);
        Color textOnDark = Color.WHITE;
        Color accent = new Color(255, 204, 0);

        Container contentPane = getContentPane();
        contentPane.setBackground(background);

        // --- Painel de Seleção de Produto e Campos ---
        JPanel topFieldsPanel = new JPanel(new BorderLayout(10, 10));
        topFieldsPanel.setBackground(Color.WHITE);
        topFieldsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Dados de Preço", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), primary),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel productSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        productSelectionPanel.setOpaque(false);
        produtoComboBox = new JComboBox<>(new String[]{});
        produtoComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(primary);
                    setForeground(textOnDark);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(text);
                }
                return this;
            }
        });
        productSelectionPanel.add(createStyledLabel("Produto:", text));
        productSelectionPanel.add(produtoComboBox);

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        fieldsPanel.setOpaque(false);
        fieldsPanel.add(createStyledLabel("Valor (ex: 5.89):", text));
        valorField = createStyledTextField();
        fieldsPanel.add(valorField);
        fieldsPanel.add(createStyledLabel("Data Alteração (dd/mm/yyyy):", text));
        dataAlteracaoField = createStyledTextField();
        fieldsPanel.add(dataAlteracaoField);

        topFieldsPanel.add(productSelectionPanel, BorderLayout.NORTH);
        topFieldsPanel.add(fieldsPanel, BorderLayout.CENTER);

        // --- Painel de Botões ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setOpaque(false);
        JButton salvarButton = createStyledButton("Salvar Novo Preço", primary, textOnDark);
        JButton excluirButton = createStyledButton("Excluir Preço", accent, text);
        buttonsPanel.add(salvarButton);
        buttonsPanel.add(excluirButton);

        // --- Tabela ---
        String[] colunas = {"ID", "Valor", "Data de Alteração"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaPrecos = new JTable(tableModel);

        // Estilo da Tabela
        tabelaPrecos.setBackground(Color.WHITE);
        tabelaPrecos.setForeground(text);
        tabelaPrecos.setGridColor(Color.LIGHT_GRAY);
        tabelaPrecos.setSelectionBackground(accent);
        tabelaPrecos.setSelectionForeground(text);
        tabelaPrecos.setFont(new Font("Arial", Font.PLAIN, 14));
        tabelaPrecos.setRowHeight(25);

        JTableHeader tableHeader = tabelaPrecos.getTableHeader();
        tableHeader.setBackground(primary);
        tableHeader.setForeground(textOnDark);
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane tableScrollPane = new JScrollPane(tabelaPrecos);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(topFieldsPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        contentPane.add(mainPanel, BorderLayout.CENTER);

        // --- Ações ---
        produtoComboBox.addActionListener(e -> carregarPrecosDoProduto());
        salvarButton.addActionListener(e -> salvarPreco());
        excluirButton.addActionListener(e -> excluirPreco());

        this.produtoMap = new HashMap<>();
        carregarProdutosComboBox();
    }

    private JLabel createStyledLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return textField;
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void carregarProdutosComboBox() {
        List<Produto> mockProdutos = new ArrayList<>();
        mockProdutos.add(new Produto(1L, "Gasolina Comum", "GC001", "Posto Exemplo", "Shell", "Combustível"));
        mockProdutos.add(new Produto(2L, "Gasolina Aditivada", "GA001", "Posto Exemplo", "Shell", "Combustível"));
        mockProdutos.add(new Produto(3L, "Etanol", "ET001", "Posto Exemplo", "Petrobras", "Combustível"));

        produtoMap = mockProdutos.stream()
            .collect(Collectors.toMap(Produto::getNome, Produto::getId));
        produtoComboBox.removeAllItems();
        produtoMap.keySet().forEach(produtoComboBox::addItem);
    }

    private void carregarPrecosDoProduto() {
        tableModel.setRowCount(0);
        String nomeProdutoSelecionado = (String) produtoComboBox.getSelectedItem();
        if (nomeProdutoSelecionado == null) return;

        Long produtoId = produtoMap.get(nomeProdutoSelecionado);
        List<Preco> filteredPrecos = precos.stream()
            .filter(p -> p.getProdutoId().equals(produtoId))
            .collect(Collectors.toList());

        for (Preco p : filteredPrecos) {
            tableModel.addRow(new Object[]{
                p.getId(),
                currencyFormat.format(p.getValor()),
                p.getDataAlteracao().format(dateFormatter)
            });
        }
    }

    private void salvarPreco() {
        try {
            String nomeProdutoSelecionado = (String) produtoComboBox.getSelectedItem();
            Long produtoId = produtoMap.get(nomeProdutoSelecionado);

            String valorTexto = valorField.getText().replaceAll("[^0-9,.]", "").replace(".", "").replace(",", ".");

            Preco novoPreco = new Preco(
                nextPrecoId++,
                new BigDecimal(valorTexto),
                LocalDate.parse(dataAlteracaoField.getText(), dateFormatter),
                produtoId
            );
            precos.add(novoPreco);
            carregarPrecosDoProduto();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar preço: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirPreco() {
        int selectedRow = tabelaPrecos.getSelectedRow();
        if (selectedRow != -1) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            precos.removeIf(p -> p.getId().equals(id));
            carregarPrecosDoProduto();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um preço na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limparCampos() {
        valorField.setText("");
        dataAlteracaoField.setText("");
    }
}
