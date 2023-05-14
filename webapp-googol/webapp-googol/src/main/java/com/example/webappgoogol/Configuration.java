package com.example.webappgoogol;

public class Configuration {
    public static final String CREDENTIALS_FILE = "credentials.bin";

    public boolean DEBUG = true;
    public static boolean COLD_START = false;
    public static boolean AUTO_FAIL_BARRELS = false;
    public static boolean AUTO_FAIL_DOWNLOADERS = false;

    // TCP ports
    public static final int PORT_A = 8082; // Had to change cause of spring-boot
    public static final int PORT_B = 8081;

    public static final int MULTICAST_PORT = 4321;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    public static final int NUM_DOWNLOADERS = 6;
    public static final int NUM_BARRELS = 4; // Even number

    public static final int CONTEXT_SIZE = 15;

    public static final int MAXIMUM_REFERENCE_LINKS = 10;

    public static final int PAGE_SIZE = 10;
}