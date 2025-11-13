import chess.*;
import java.net.URI;
import java.util.Objects;
import java.util.Scanner;
import java.net.http.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {

            new MainBackground(serverUrl).running();
        }
        catch (Exception e) {
            System.out.printf("Error: Unable to start server: %s%n", e.getMessage());
        }




        return;
    }
}