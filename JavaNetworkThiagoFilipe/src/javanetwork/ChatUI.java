package javanetwork;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ChatUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ── Cores ──────────────────────────────────────────────────────────────
    private static final Color COR_FUNDO        = new Color(18, 18, 18);
    private static final Color COR_TOPO         = new Color(30, 30, 30);
    private static final Color COR_BALAO_EU     = new Color(0, 132, 255);
    private static final Color COR_BALAO_OUTRO  = new Color(45, 45, 45);
    private static final Color COR_TEXTO_EU     = Color.WHITE;
    private static final Color COR_TEXTO_OUTRO  = new Color(220, 220, 220);
    private static final Color COR_NOME         = new Color(150, 150, 150);
    private static final Color COR_INPUT_FUNDO  = new Color(30, 30, 30);
    private static final Color COR_INPUT_TEXTO  = Color.WHITE;
    private static final Color COR_BOTAO        = new Color(0, 132, 255);
    private static final Color COR_RODAPE       = new Color(25, 25, 25);
    private static final Color COR_STATUS_OK    = new Color(72, 199, 142);
    private static final Color COR_STATUS_ERR   = new Color(220, 80, 80);

    // ── Componentes ────────────────────────────────────────────────────────
    private JPanel       painelMensagens;
    private JScrollPane  scroll;
    private JTextField   campoTexto;
    private JButton      botaoEnviar;
    private JLabel       labelStatus;
    private JLabel       labelNomePar;
    private JLabel       avatar;

    // ── Estado de rede ─────────────────────────────────────────────────────
    private PrintStream  saida;
    private String       meuNome;
    private String       nomePar = "...";
    private boolean      conectado = false;

    // ─────── Construtor ─────────────────────────────────────────────────────
    public ChatUI(String meuNome, Socket socketSaida, Socket socketEntrada,
                  String nomeParInicial) {
        this.meuNome = meuNome;
        this.nomePar = nomeParInicial;

        construirUI();
        setVisible(true);

        // Inicializa saída
        try {
            saida = new PrintStream(socketSaida.getOutputStream());
        } catch (IOException e) {
            mostrarErro("Erro ao abrir stream de saída: " + e.getMessage());
            return;
        }

        // Thread de recebimento → adiciona balão do outro lado
        ThreadRecebimento recv = new ThreadRecebimento(
            socketEntrada,
            linha -> {
                // Primeira mensagem pode ser o nome do par (protocolo simples)
                if (linha.startsWith("__NOME__:")) {
                    String nomeRecebido = linha.substring(9);
                    SwingUtilities.invokeLater(() -> {
                        nomePar = nomeRecebido;
                        labelNomePar.setText(nomePar);
                        avatar.repaint();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> adicionarBalao(linha, nomePar, false));
                }
            },
            () -> SwingUtilities.invokeLater(() -> {
                conectado = false;
                labelStatus.setText("● Desconectado");
                labelStatus.setForeground(COR_STATUS_ERR);
                campoTexto.setEnabled(false);
                botaoEnviar.setEnabled(false);
                adicionarAviso("O par desconectou.");
            })
        );
        recv.setDaemon(true);
        recv.start();

        // Envia o próprio nome para o par identificar
        saida.println("__NOME__:" + meuNome);

        conectado = true;
        labelStatus.setText("● Conectado");
        labelStatus.setForeground(COR_STATUS_OK);
        campoTexto.setEnabled(true);
        botaoEnviar.setEnabled(true);
        campoTexto.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════════════
    private void construirUI() {
        setTitle("Chat — " + meuNome);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 650);
        setMinimumSize(new Dimension(340, 480));
        setLocationRelativeTo(null);
        setBackground(COR_FUNDO);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarTopo(),   BorderLayout.NORTH);
        raiz.add(criarCorpo(),  BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
    }

    // ── Topo ───────────────────────────────────────────────────────────────
    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(COR_TOPO);
        topo.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Avatar circular
        avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BALAO_EU);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String inicial = nomePar.isEmpty() ? "?" : String.valueOf(nomePar.charAt(0)).toUpperCase();
                g2.drawString(inicial,
                    (getWidth()  - fm.stringWidth(inicial)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(COR_TOPO);
        info.setBorder(new EmptyBorder(0, 12, 0, 0));

        labelNomePar = new JLabel(nomePar);
        labelNomePar.setForeground(Color.WHITE);
        labelNomePar.setFont(new Font("SansSerif", Font.BOLD, 15));

        labelStatus = new JLabel("● Aguardando...");
        labelStatus.setForeground(COR_NOME);
        labelStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));

        info.add(labelNomePar);
        info.add(Box.createVerticalStrut(2));
        info.add(labelStatus);

        topo.add(avatar, BorderLayout.WEST);
        topo.add(info,   BorderLayout.CENTER);

        // Separador
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(COR_FUNDO);
        wrap.add(topo, BorderLayout.CENTER);
        wrap.add(new JSeparator() {{ setForeground(new Color(50,50,50)); }}, BorderLayout.SOUTH);
        return wrap;
    }

    // ── Área de mensagens ──────────────────────────────────────────────────
    private JScrollPane criarCorpo() {
        painelMensagens = new JPanel();
        painelMensagens.setLayout(new BoxLayout(painelMensagens, BoxLayout.Y_AXIS));
        painelMensagens.setBackground(COR_FUNDO);
        painelMensagens.setBorder(new EmptyBorder(12, 10, 12, 10));

        scroll = new JScrollPane(painelMensagens);
        scroll.setBackground(COR_FUNDO);
        scroll.getViewport().setBackground(COR_FUNDO);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Rodapé (campo de envio) ────────────────────────────────────────────
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(10, 0));
        rodape.setBackground(COR_RODAPE);
        rodape.setBorder(new EmptyBorder(10, 14, 12, 14));

        campoTexto = new JTextField();
        campoTexto.setBackground(COR_INPUT_FUNDO);
        campoTexto.setForeground(COR_INPUT_TEXTO);
        campoTexto.setCaretColor(Color.WHITE);
        campoTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campoTexto.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campoTexto.setEnabled(false);
        campoTexto.addActionListener(e -> enviar());

        botaoEnviar = new JButton("Enviar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? COR_BOTAO : new Color(60, 60, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        botaoEnviar.setForeground(Color.WHITE);
        botaoEnviar.setFont(new Font("SansSerif", Font.BOLD, 13));
        botaoEnviar.setFocusPainted(false);
        botaoEnviar.setBorderPainted(false);
        botaoEnviar.setContentAreaFilled(false);
        botaoEnviar.setOpaque(false);
        botaoEnviar.setPreferredSize(new Dimension(80, 38));
        botaoEnviar.setEnabled(false);
        botaoEnviar.addActionListener(e -> enviar());

        rodape.add(campoTexto,  BorderLayout.CENTER);
        rodape.add(botaoEnviar, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(COR_FUNDO);
        wrap.add(new JSeparator() {{ setForeground(new Color(50,50,50)); }}, BorderLayout.NORTH);
        wrap.add(rodape, BorderLayout.CENTER);
        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════════
    private void enviar() {
        String texto = campoTexto.getText().trim();
        if (texto.isEmpty() || !conectado) return;
        saida.println(texto);
        adicionarBalao(texto, meuNome, true);
        campoTexto.setText("");
    }

    // ── Balão de mensagem ──────────────────────────────────────────────────
    private void adicionarBalao(String texto, String nome, boolean souEu) {
        JPanel linha = new JPanel(new FlowLayout(
            souEu ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        linha.setBackground(COR_FUNDO);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel balao = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(souEu ? COR_BALAO_EU : COR_BALAO_OUTRO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        balao.setLayout(new BoxLayout(balao, BoxLayout.Y_AXIS));
        balao.setOpaque(false);
        balao.setBorder(new EmptyBorder(8, 12, 8, 12));

        if (!souEu) {
            JLabel lblNome = new JLabel(nome);
            lblNome.setForeground(COR_NOME);
            lblNome.setFont(new Font("SansSerif", Font.BOLD, 11));
            lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);
            balao.add(lblNome);
            balao.add(Box.createVerticalStrut(3));
        }

        // Quebra o texto em linhas para não estourar a largura
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(souEu ? COR_TEXTO_EU : COR_TEXTO_OUTRO);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));
        area.setPreferredSize(null);
        area.setSize(new Dimension(260, Short.MAX_VALUE));
        area.setPreferredSize(new Dimension(
            Math.min(260, area.getPreferredSize().width + 4),
            area.getPreferredSize().height));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        balao.add(area);

        // Limita largura máxima do balão
        balao.setMaximumSize(new Dimension(290, Short.MAX_VALUE));

        linha.add(balao);
        painelMensagens.add(linha);
        painelMensagens.add(Box.createVerticalStrut(6));
        painelMensagens.revalidate();

        // Scroll até o fim
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = scroll.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    // ── Aviso de sistema ───────────────────────────────────────────────────
    private void adicionarAviso(String texto) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linha.setBackground(COR_FUNDO);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel label = new JLabel(texto);
        label.setForeground(COR_NOME);
        label.setFont(new Font("SansSerif", Font.ITALIC, 12));
        linha.add(label);

        painelMensagens.add(linha);
        painelMensagens.revalidate();
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tela de login
    // ══════════════════════════════════════════════════════════════════════
    public static void mostrarLogin(int minhaPorta, String ip, int portaPar, String nomeParPadrao) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Entrar no chat");
        dialog.setModal(true);
        dialog.setSize(340, 220);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(new Color(25, 25, 25));
        painel.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel titulo = new JLabel("Como quer ser chamado?");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField campoNome = new JTextField(20);
        campoNome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campoNome.setBackground(new Color(40, 40, 40));
        campoNome.setForeground(Color.WHITE);
        campoNome.setCaretColor(Color.WHITE);
        campoNome.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campoNome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel labelInfo = new JLabel(" ");
        labelInfo.setForeground(COR_STATUS_ERR);
        labelInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton botao = new JButton("Entrar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BOTAO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font("SansSerif", Font.BOLD, 13));
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);

        ActionListener entrar = e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                labelInfo.setText("Digite um nome para continuar.");
                return;
            }
            dialog.dispose();

            // Conecta em background para não travar a EDT
            new Thread(() -> {
                try {
                    ServidorThread servidor = new ServidorThread(minhaPorta);
                    servidor.setDaemon(true);
                    servidor.start();

                    Socket socketSaida = conectarComRetry(ip, portaPar, 30);
                    Socket socketEntrada = servidor.getSocket();

                    SwingUtilities.invokeLater(() ->
                        new ChatUI(nome, socketSaida, socketEntrada, nomeParPadrao));

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null,
                            "Erro de conexão: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        };

        botao.addActionListener(entrar);
        campoNome.addActionListener(entrar);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(18));
        painel.add(campoNome);
        painel.add(Box.createVerticalStrut(6));
        painel.add(labelInfo);
        painel.add(Box.createVerticalStrut(12));
        painel.add(botao);

        dialog.setContentPane(painel);
        dialog.setVisible(true);
    }

    private static Socket conectarComRetry(String ip, int porta, int tentativas)
            throws IOException, InterruptedException {
        for (int i = 1; i <= tentativas; i++) {
            try {
                return new Socket(ip, porta);
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        throw new IOException("Não foi possível conectar em " + ip + ":" + porta);
    }
}