package client;
import chess.*;
import client.ui.MainHelper;
import com.google.gson.Gson;
import static client.ui.EscapeSequences.*;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.net.http.*;

public class Main {

    static MainHelper help = new MainHelper();
    public static void main(String[] args) throws Exception {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        try {
            var bg = new MainBackground(serverUrl);

            String logcase = "Logged Out";
            System.out.println("♕ 240 Chess Client\n");
            Gson gson = new Gson();
            boolean finished = false;
            help.helper(finished, logcase, serverUrl, gson, bg);
        }
        catch (Exception e) {
            System.out.printf("Error: Unable to start server: %s%n", e.getMessage());
        }
        return;
    }
}
