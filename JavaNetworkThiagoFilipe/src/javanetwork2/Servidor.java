package javanetwork2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) {
    	// try-with-resources: abre todos os recursos e fecha automaticamente ao sair do bloco
        try (
            ServerSocket servidor = new ServerSocket(10000);
            Socket cliente = servidor.accept();
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintStream saida = new PrintStream(cliente.getOutputStream());
            Scanner teclado = new Scanner(System.in)
        ) {
            System.out.println("Cliente: "+cliente.getInetAddress().getHostAddress()+"conectado!");

            
            // thread leitor: responsabilidade única de ficar escutando mensagens do cliente
            // roda em paralelo com a thread main, cada uma travada no seu próprio ponto de espera
            Thread leitor = new Thread(new Runnable(){
                @Override
                public void run() {
                    String linha;
                    try {
                    	// readLine() bloqueia aqui esperando mensagem chegar
                        // retorna null quando a conexão cai, encerrando o loop
                        while ((linha = entrada.readLine()) != null) {
                            System.out.println(linha);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // daemon: quando a thread main encerrar, essa thread morre automaticamente junto
            // sem isso o programa ficaria travado esperando ela terminar
            leitor.setDaemon(true);
            leitor.start();// inicia a thread leitor — a partir daqui as duas rodam ao mesmo tempo
            
            
            // thread main: responsabilidade única de ler o teclado e enviar ao cliente
            // hasNextLine() bloqueia aqui esperando o usuário digitar
            while (teclado.hasNextLine()) { 
                String linha = teclado.nextLine();
                saida.println(linha);// envia a linha digitada ao cliente
            }
            
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}