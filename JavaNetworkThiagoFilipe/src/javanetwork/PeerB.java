package javanetwork;

import javax.swing.*;

public class PeerB {

    private static final String IP       = "127.0.0.1";
    private static final int MINHA_PORTA = 10001;
    private static final int PORTA_PAR   = 10000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            ChatUI.mostrarLogin(MINHA_PORTA, IP, PORTA_PAR, "PeerA"));
    }
}