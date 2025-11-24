package client;
import chess.*;
import com.google.gson.Gson;
import static client.ui.EscapeSequences.*;

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

            var bg = new MainBackground(serverUrl);


            System.out.println("♕ 240 Chess Client\n");
            Gson gson = new Gson();
            boolean finished = false;
            while (!finished) {
                System.out.printf(SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_YELLOW + "What is your command?" +
                        SET_BG_COLOR_BLACK + "\n");
                var scanner = new Scanner(System.in);
                var result = scanner.next();

                switch (result) {
                    case ("exit") -> {
                        finished = true;
                    }
                    case ("register"), ("login") -> {
                        bg.addUser(result);
                    }
                    case ("logout") -> {
                        bg.logoutUser();
                    }
                    case ("list"), ("create"), ("join") -> {
                        bg.gameAction(result);
                    }
                    case ("help") -> {
                        bg.help();
                    }
                    case ("clear") -> {

                    }
                    default -> {
                        System.out.println(result + " is not a valid command, use help to see all valid commands\n");
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.printf("Error: Unable to start server: %s%n", e.getMessage());
        }




        return;
    }
}