package javanetwork2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Peer1 {

    public static void main(String[] args) {

        Socket socket = null;

        try {
            // tenta conectar como cliente — se alguém já estiver escutando, funciona
            System.out.println("Tentando conectar...");
            socket = new Socket("127.0.0.1", 10000);
            System.out.println("Conectado como cliente!");

        } catch (ConnectException e) {
            // ninguém está escutando — sobe como servidor
            System.out.println("Ninguém encontrado, subindo como servidor...");
            try {
                ServerSocket servidor = new ServerSocket(10000);
                socket = servidor.accept();
                servidor.close();
                System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // a partir daqui é igual para os dois lados
        if (socket != null) {
            try (
                Socket s = socket;
                BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream saida = new PrintStream(s.getOutputStream());
                Scanner teclado = new Scanner(System.in)
            ) {
                Thread leitor = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String linha;
                        try {
                            while ((linha = entrada.readLine()) != null) {
                                System.out.println(linha);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                leitor.setDaemon(true);
                leitor.start();

                while (teclado.hasNextLine()) {
                    String linha = teclado.nextLine();
                    saida.println(linha);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}