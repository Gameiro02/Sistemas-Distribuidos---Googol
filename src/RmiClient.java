package src;

import java.util.*;
import java.rmi.Naming;

import src.SearchModule.SearchModuleInterface;

public class RmiClient {

    public static void main(String[] args) throws Exception {

        SearchModuleInterface searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");

        printMenu();

        System.out.print("Digite o comando desejado: ");
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        while (true) {
            int command = scanner.nextInt();
            switch (command) {
                case 1:
                    System.out.print("Insira o link da página: ");
                    scanner.nextLine();
                    String url = scanner.nextLine();
                    break;
                case 2:
                    System.out.print("Insira os termos a serem pesquisados: ");
                    scanner.nextLine();
                    String words = scanner.nextLine();
                    System.out.println(searchModule.searchForWords(words));
                    break;
                case 3:
                    System.out.print("Insira o link da página: ");
                    scanner.nextLine();
                    String link = scanner.nextLine();
                    System.out.println(searchModule.linksToAPage(link));
                    break;
                case 4:
                    System.out.println(searchModule.getStringMenu());
                    break;
                case 5:
                    exit = true;
                    break;
                case 6:
                    printMenu();
                    break;
                default:
                    System.out.println("Comando inválido");
                    break;
            }
            if (exit) {
                break;
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println(" ========================================MENU========================================");
        System.out.println("| Comando:      Descrição:                                                           |");
        System.out.println("| -----------------------------------------------------------------------------------|");
        System.out.println("| 1.            Indexar um novo URL                                                  |");
        System.out.println("| 2.            Pesquisar paginas que contenham um conjunto de termos                |");
        System.out.println("| 3.            Consultar listas de paginas com ligação para um página expecifica    |");
        System.out.println("| 4.            Abrir página de administração atualizada em tempo real               |");
        System.out.println("| 5.            Sair                                                                 |");
        System.out.println("| 6.            Mostrar o menu                                                       |");
        System.out.println(" ====================================================================================");
    }
}
