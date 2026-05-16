package javanetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Roda em background esperando a outra máquina se conectar.
 * Quando aceita uma conexão, entrega o Socket para quem chamou
 * via getSocket() (bloqueia até a conexão chegar).
 */
public class ServidorThread extends Thread {

    private final int porta;
    private Socket socketCliente;
    private final Object lock = new Object();

    public ServidorThread(int porta) {
        this.porta = porta;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(porta)) {
            System.out.println("[Servidor] Aguardando conexão na porta " + porta + "...");
            Socket s = server.accept();
            System.out.println("[Servidor] Conectado: " + s.getInetAddress().getHostAddress());

            synchronized (lock) {
                this.socketCliente = s;
                lock.notifyAll(); // avisa quem estiver esperando
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Erro: " + e.getMessage());
        }
    }

    /** Bloqueia até a conexão ser estabelecida e retorna o Socket. */
    public Socket getSocket() throws InterruptedException {
        synchronized (lock) {
            while (socketCliente == null) {
                lock.wait();
            }
            return socketCliente;
        }
    }
}