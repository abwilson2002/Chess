
import chess.*;
import org.eclipse.jetty.websocket.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Scanner;
import server.src.main.*;

public class MainBackground {

    private String user = null;
    boolean signedIn = false;
    private Server chessServer;
    private String userAuth = "";
    private HttpClient httpClient = HttpClient.newHttpClient();

    public MainBackground(String serverUrl) throws Exception {

        chessServer = new Server(serverUrl);

    }

    public void running() {
        System.out.println("♕ 240 Chess Client\n");
        System.out.println("What is your command?");

        boolean finished = false;
        while (!finished) {
            var scanner = new Scanner(System.in);
            var result = scanner.nextLine();

            switch(result) {
                case ("exit") -> {
                    finished = true;
                }
                case ("register") -> {
                    chessServer.get();
                }
                case ("login") -> {

                }
                case ("logout") -> {

                }
                case ("list") -> {

                }
                case ("create") -> {

                }
                case ("join") -> {

                }
            }
        }


    }

}
