package src;

public class Configuration {
    public static final String LOGIN = "tintin";
    public static final String PASSWORD = "unicorn";
    public static final String CREDENTIALS_FILE = "credentials.bin";

    public boolean DEBUG = true;
    public static boolean COLD_START = true;

    // TCP ports
    public static final int PORT_A = 8080;
    public static final int PORT_B = 8081;

    public static final int MULTICAST_PORT = 4321;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    public static final int NUM_DOWNLOADERS = 10;
    public static final int NUM_BARRELS = 2;

    // Multicast para a pagina de administração
    public static final String MULTICAST_ADDRESS_ADMIN = "224.3.2.2";
    public static final int MULTICAST_PORT_ADMIN = 4333;
    public static final int CONTEXT_SIZE = 15;

    public static final int MAXIMUM_REFERENCE_LINKS = 10;
}