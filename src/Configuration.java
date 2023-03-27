package src;

public class Configuration {
    public static final String LOGIN = "tintin";
    public static final String PASSWORD = "unicorn";
    public static final String CREDENTIALS_FILE = "credentials.bin";

    public boolean DEBUG = true;

    // TCP ports
    public static final int PORT_A = 8080;
    public static final int PORT_B = 8081;

    public static final int MULTICAST_PORT = 4321;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    public static final int NUM_DOWNLOADERS = 2;
    public static final int NUM_BARRELS = 3;

    // Multicast para a pagina de administração
    public static final String MULTICAST_ADDRESS_ADMIN = "224.3.2.2";
    public static final int MULTICAST_PORT_ADMIN = 4333;
    public static final int CONTEXT_SIZE = 15;
}