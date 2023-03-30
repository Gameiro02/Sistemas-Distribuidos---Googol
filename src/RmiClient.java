package src;

import java.util.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.*;
import java.net.MalformedURLException;

import src.SearchModule.SearchModuleInterface;

public class RmiClient {
    private boolean login;

    private void run(SearchModuleInterface searchModule) throws Exception {
        printMenu();

        System.out.print("Digite o comando desejado: ");
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (true) {
            int command;
            try {
                command = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.err.println("Comando inválido");
                System.out.print("Digite o comando desejado: ");
                scanner.next();
                continue;
            }
            switch (command) {
                case 1:
                    System.out.print("Insira o link da página: ");
                    scanner.nextLine();
                    String url = scanner.nextLine();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        System.out.println("Link inválido");
                        System.out.println("Tem que começar com http:// ou https://");
                        System.out.println(
                                "====================================================================================");
                        System.out.println("Digite 6 para exibir o menu novamente");
                        System.out.print("Digite o comando desejado: ");
                        break;
                    }
                    searchModule.IndexarUmNovoUrl(url);
                    System.out.println("Página enviada para a fila de indexação");
                    System.out.println(
                            "====================================================================================");
                    System.out.println("Digite 6 para exibir o menu novamente");
                    System.out.print("Digite o comando desejado: ");
                    break;
                case 2:
                    searchWord(searchModule, scanner);
                    break;
                case 3:
                    System.out.println(
                            "====================================================================================");
                    checkLogin(searchModule, scanner, 3);
                    System.out.println(
                            "====================================================================================");
                    System.out.println("Digite 6 para exibir o menu novamente");
                    System.out.print("Digite o comando desejado: ");
                    break;
                case 4:
                    System.out.println(
                            "====================================================================================");
                    System.out.println(searchModule.getStringMenu());
                    System.out.println(
                            "====================================================================================");
                    System.out.println("Digite 6 para exibir o menu novamente");
                    System.out.print("Digite o comando desejado: ");
                    break;
                case 5:
                    exit = true;
                    break;
                case 6:
                    printMenu();
                    break;
                case 7:
                    checkLogin(searchModule, scanner, 7);
                    System.out.println("Digite 6 para exibir o menu novamente");
                    System.out.print("Digite o comando desejado: ");
                    break;
                default:
                    System.out.println("Comando inválido");
                    System.out.print("Digite o comando desejado: ");
                    break;
            }
            if (exit) {
                break;
            }
        }
        scanner.close();
    }

    private void searchWord(SearchModuleInterface searchModule, Scanner scanner)
            throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException {

        System.out.print("Insira os termos a serem pesquisados: ");
        scanner.nextLine();
        String words = scanner.nextLine();

        int pageNumber = 1;
        List<String> resultList = searchModule.searchForWords(words, pageNumber);
        int i = 0;
        while (true) {
            System.out.println("====================================================================================");
            System.out.println("Resultados da pesquisa:");

            if (resultList.isEmpty() && pageNumber == 1) {
                System.out.println("Nenhum resultado encontrado");
                System.out.println(
                        "====================================================================================");
                System.out.println("Digite 6 para exibir o menu novamente");
                System.out.print("Digite o comando desejado: ");
                break;
            } else if (resultList.isEmpty()) {
                System.out.println("Não há mais resultados");
                System.out.println(
                        "====================================================================================");
                System.out.println("Digite 6 para exibir o menu novamente");
                System.out.print("Digite o comando desejado: ");
                break;
            }

            boolean info = false;

            for (int j = i; j < resultList.size(); j++) {
                if (j == pageNumber * 10)
                    break;
                String fields[] = resultList.get(j).split(";");
                System.out.println("Link: " + fields[0]);
                System.out.println("Título: " + fields[1]);
                System.out.println("Descrição: " + fields[2] + "\n");
                info = true;
                i++;
            }

            // If i not multiple of
            if (!info || i % 10 != 0) {
                System.out.println("Não há mais resultados");
                System.out.println(
                        "====================================================================================");
                System.out.println("Digite 6 para exibir o menu novamente");
                System.out.print("Digite o comando desejado: ");
                return;
            }

            System.out.println("====================================================================================");

            System.out.println("Próxima página? (s/n)");
            String next = scanner.nextLine();
            if (next.equals("s"))
                pageNumber++;
            else {
                System.out.println(
                        "====================================================================================");
                System.out.println("Digite 6 para exibir o menu novamente");
                System.out.print("Digite o comando desejado: ");
                return;
            }
        }
    }

    private void checkLogin(SearchModuleInterface searchModule, Scanner scanner, int command)
            throws FileNotFoundException, IOException, NotBoundException {
        boolean justLogged = false;
        if (!login) {
            if (command == 3)
                System.out.println("Para utilizar esta função, é necessário estar logado");
            System.out.print("Insira o nome de usuário: ");
            scanner.nextLine();
            String username = scanner.nextLine();
            System.out.print("Insira a senha: ");
            String password = scanner.nextLine();

            if (searchModule.login(username, password)) {
                System.out.println("Login realizado com sucesso");
                System.out.println(
                        "====================================================================================");
                login = true;
                justLogged = true;
            } else {
                System.out.println("Login inválido");
                return;
            }
        }

        if (login && command == 3) {
            if (!justLogged)
                scanner.nextLine();
            System.out.print("Insira o link da página: ");
            String url = scanner.nextLine();
            List<String> links = (searchModule.linksToAPage(url));

            if (links.isEmpty()) {
                System.out.println("Nenhum resultado encontrado");
            } else {
                System.out.println("Resultados da pesquisa:");
                for (String link : links) {
                    System.out.println(link);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        boolean connected = false;
        SearchModuleInterface searchModule = null;
        while (!connected) {
            try {
                searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");
                connected = true;
            } catch (Exception e) {
                System.out.println("Erro ao conectar com o servidor, tentando novamente em 3 segundos");
                Thread.sleep(3000);
            }
        }

        RmiClient client = new RmiClient();
        client.login = false;

        try {
            client.run(searchModule);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor, tentando novamente em 3 segundos");
            Thread.sleep(3000);
            main(args);
        }

    }

    private void printMenu() {
        System.out.println(" ========================================MENU========================================");
        System.out.println("| Comando:      Descrição:                                                           |");
        System.out.println("| -----------------------------------------------------------------------------------|");
        System.out.println("| 1.            Indexar um novo URL                                                  |");
        System.out.println("| 2.            Pesquisar paginas que contenham um conjunto de termos                |");
        System.out.println("| 3.            Consultar listas de paginas com ligação para um página expecifica    |");
        System.out.println("| 4.            Abrir página de administração atualizada em tempo real               |");
        System.out.println("| 5.            Sair                                                                 |");
        System.out.println("| 6.            Mostrar o menu                                                       |");
        System.out.println("| 7.            Login                                                                |");
        System.out.println(" ====================================================================================");
    }

}
