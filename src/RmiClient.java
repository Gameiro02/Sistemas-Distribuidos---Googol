package src;

import java.util.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.io.*;

import src.SearchModule.SearchModuleInterface;

public class RmiClient {
    private boolean login;

    private void run(SearchModuleInterface searchModule) throws Exception {
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
                    checkLogin(searchModule, scanner);
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
                case 7:
                    checkLogin(searchModule, scanner);
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

    private void checkLogin(SearchModuleInterface searchModule, Scanner scanner)
            throws FileNotFoundException, IOException, NotBoundException {
        if (!login) {
            System.out.println("Para utilizar esta função, é necessário estar logado");
            System.out.print("Insira o nome de usuário: ");
            scanner.nextLine();
            String username = scanner.nextLine();
            System.out.print("Insira a senha: ");
            String password = scanner.nextLine();

            if (isValid(username, password, Configuration.CREDENTIALS_FILE)) {
                System.out.println("Login realizado com sucesso");
                login = true;
            } else {
                System.out.println("Login inválido");
            }

        } else {
            System.out.print("Insira o link da página: ");
            scanner.nextLine();
            String link = scanner.nextLine();
            System.out.println(searchModule.linksToAPage(link));
        }
    }

    public static void main(String[] args) throws Exception {
        SearchModuleInterface searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");

        RmiClient client = new RmiClient();
        client.login = false;

        client.run(searchModule);

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

    public boolean isValid(String username, String password, String filename) {
        boolean valid = false;
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            while (true) {
                String user;
                String pass;

                // read user and password
                user = (String) in.readObject();
                pass = (String) in.readObject();

                if (user.equals(username) && pass.equals(password)) {
                    valid = true;
                    in.close();
                    break;
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler arquivo de credenciais");
        }
        return valid;
    }

}
