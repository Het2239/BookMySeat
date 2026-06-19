package com.bookmyseat;

import com.bookmyseat.db.DBConnection;
import com.bookmyseat.menu.MainMenu;

/**
 * BookMySeat — Application Entry Point
 * Run: mvn compile exec:java -Dexec.mainClass=com.bookmyseat.Main
 * Or:  java -jar target/BookMySeat-1.0-SNAPSHOT.jar
 */
public class Main {

    public static void main(String[] args) {
        // Register shutdown hook to close the connection pool cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            DBConnection.getInstance().shutdown()
        ));

        new MainMenu().start();
    }
}
