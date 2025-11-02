package app;

import app.config.ApplicationConfig;
import app.populator.Populator;

public class Main {

    public static void main(String[] args) {

        ApplicationConfig.startServer(7007);

        Populator.populate();
    }
}