package javanetwork2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {
	public static void main(String[] args) {
		
		
		// try-with-resources: abre todos os recursos e fecha automaticamente ao sair do bloco
         try (
        		 Socket servidor = new Socket("127.0.0.1",10000);
        		 BufferedReader entrada = new BufferedReader(new InputStreamReader(servidor.getInputStream()));
                 PrintStream saida = new PrintStream(servidor.getOutputStream());
        		 Scanner teclado = new Scanner(System.in))
         {
             Thread leitor = new Thread(new Runnable(){
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
             leitor.setDaemon(true);// thread auxiliar acaba quando todas as threads não-daemon acabarem
             leitor.start();
             
             while (teclado.hasNextLine()) { // thread main 
                 String linha = teclado.nextLine();
                 saida.println(linha);
             }
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
