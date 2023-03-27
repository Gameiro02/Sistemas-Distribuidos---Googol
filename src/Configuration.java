package src;

public class Configuration {
    public static final String login = "tintin";
    public static final String password = "unicorn";

    public boolean DEBUG = true;

    // TCP ports
    public static int PORT_A = 8080;
    public static int PORT_B = 8081;

    public static int MULTICAST_PORT = 4321;
    public static String MULTICAST_ADDRESS = "224.3.2.1";

    public static int NUM_DOWNLOADERS = 2;
    public static int NUM_BARRELS = 3;

    // Multicast para a pagina de administração
    public static String MULTICAST_ADDRESS_ADMIN = "224.3.2.2";
    public static int MULTICAST_PORT_ADMIN = 4333;
    public static int CONTEXT_SIZE = 15;
}
