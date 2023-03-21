package src;

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;

public class RmiClient {

    public static void main(String[] args) {
        System.out.println(" ========================================MENU========================================");
        System.out.println("| Comando:      Descrição:                                                           |");
        System.out.println("| -----------------------------------------------------------------------------------|");
        System.out.println("| 1.            Indexar um novo URL                                                  |");
        System.out.println("| 2.            Pesquisar paginas que contenham um conjunto de termos                |");
        System.out.println("| 3.            Consultar listas de paginas com ligação para um página expecifica    |");
        System.out.println("| 4.            Abrir página de administração atualizada em tempo real               |");
        System.out.println("| 5.            Sair                                                                 |");
        System.out.println(" ====================================================================================");

        System.out.println("Digite o comando desejado: ");
        Scanner scanner = new Scanner(System.in);

        int command = scanner.nextInt();

        switch (command) {
            case 1:
                System.out.println("Opcao selecionada: 1 - Indexar um novo URL");
                break;
            case 2:
                System.out.println("Opcao selecionada: 2 - Pesquisar paginas que contenham um conjunto de termos");
                break;
            case 3:
                System.out.println(
                        "Opcao selecionada: 3 - Consultar listas de paginas com ligação para um página expecifica");
                break;
            case 4:
                System.out.println("Opcao selecionada: 4 - Abrir página de administração atualizada em tempo real");
                break;
            case 5:
                System.out.println("Opcao selecionada: 5 - Sair");
                return;
            default:
                System.out.println("Comando inválido");
                break;
        }
    }
}
