package javanetwork;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Fica bloqueada esperando mensagens chegarem pelo socket.
 * Em vez de imprimir no console, chama o callback onMensagem
 * para que a UI possa exibir a mensagem.
 */
public class ThreadRecebimento extends Thread {

    private final Socket socket;
    private final Consumer<String> onMensagem;
    private final Runnable onDesconectado;

    public ThreadRecebimento(Socket socket,
                             Consumer<String> onMensagem, Runnable onDesconectado) {
        this.socket         = socket;
        this.onMensagem     = onMensagem;
        this.onDesconectado = onDesconectado;
    }

    @Override
    public void run() {
        try (Scanner entrada = new Scanner(socket.getInputStream())) {
            while (entrada.hasNextLine()) {
                String linha = entrada.nextLine();
                onMensagem.accept(linha);
            }
        } catch (IOException e) {
            // conexão caiu
        } finally {
            if (onDesconectado != null) onDesconectado.run();
        }
    }
}