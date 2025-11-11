
import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class MainBackground {

    private static final Logger log = LoggerFactory.getLogger(MainBackground.class);
    private String user = null;
    boolean signedIn = false;
    private String userAuth = "";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;
    boolean loggedIn = false;

    public MainBackground(String serverName) throws Exception {

        serverUrl = serverName;

    }

    public void running() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("♕ 240 Chess Client\n");
        Gson gson = new Gson();

        boolean finished = false;
        while (!finished) {
            System.out.println("What is your command?");
            var scanner = new Scanner(System.in);
            var result = scanner.next();

            /*HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(serverUrl))
                    .header("Authorization", userAuth)
                    .timeout(java.time.Duration.ofMillis(5000))
                    .GET()
                    .build();
            */

            switch(result) {
                case ("exit") -> {
                    finished = true;
                }
                case ("register"), ("login") -> {
                    String pathedUrl;
                    String requestInput;

                    if (result.equals("register")) {
                        pathedUrl = serverUrl + "/user";
                        String username = scanner.next();
                        String pass = scanner.next();
                        String email = scanner.next();

                        var input = Map.of("username", username, "password", pass, "email", email);
                        requestInput = gson.toJson(input);
                    } else {
                        pathedUrl = serverUrl + "/session";
                        String username = scanner.next();
                        String pass = scanner.next();

                        var input = Map.of("username", username, "password", pass);
                        requestInput = gson.toJson(input);
                    }


                    var request = HttpRequest.newBuilder()
                            .uri(new URI(pathedUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    String responseBody = response.body();

                    AuthData output = gson.fromJson(responseBody, AuthData.class);

                    userAuth = output.authToken();
                    user = output.username();

                    System.out.println("Logged in as: " + user);
                    loggedIn = true;
                }
                case ("logout") -> {
                    String registerUrl = serverUrl + "/session";

                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .DELETE()
                            .build();

                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    System.out.println("Logged out");

                    loggedIn = false;
                }
                case ("list"), ("create"), ("join") -> {
                    if (!loggedIn) {
                        System.out.println("Please log in or register before continuing");
                        break;
                    }
                    String registerUrl = serverUrl + "/game";
                    switch(result) {
                        case ("list") -> {
                            var request = HttpRequest.newBuilder()
                                    .uri(new URI(registerUrl))
                                    .header("Authorization", userAuth)
                                    .timeout(java.time.Duration.ofMillis(5000))
                                    .GET()
                                    .build();

                            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                            String responseBody = response.body();

                            List output = gson.fromJson(responseBody, List.class);
                        }
                        case ("create") -> {

                            String gameName = scanner.next();

                            var input = Map.of("username", user, "gameName", gameName);
                            String requestInput = gson.toJson(input);

                            var request = HttpRequest.newBuilder()
                                    .uri(new URI(registerUrl))
                                    .header("Authorization", userAuth)
                                    .timeout(java.time.Duration.ofMillis(5000))
                                    .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                                    .build();

                            System.out.println("Game created");
                        }
                        case ("join") -> {

                            String gameName = scanner.next();

                            String playerColor = scanner.next();

                            var input = Map.of("username", user, "gameName", gameName, "playerColor", playerColor);
                            String requestInput = gson.toJson(input);


                            var request = HttpRequest.newBuilder()
                                    .uri(new URI(registerUrl))
                                    .header("Authorization", userAuth)
                                    .timeout(java.time.Duration.ofMillis(5000))
                                    .PUT(HttpRequest.BodyPublishers.ofString(requestInput))
                                    .build();
                        }
                    }
                }
                case ("help") -> {
                    if (!loggedIn) {
                        System.out.println("register <username> <password> <email> : this registers a new user with the given credentials");
                        System.out.println("login <username> <password> : this logs you in as a preexisting user");
                        System.out.println("exit : this ends the chess client");
                    } else {
                        System.out.println("logout : this logs you out");
                        System.out.println("list : this will list all of the games currently saved");
                        System.out.println("create <gameName> : this will create a new game with the given name");
                        System.out.println("join <gameID> <color> : this will join you as a player or spectator depending on the your input (enter BLUE to be a spectator");
                    }
                }
                case ("clear") -> {
                    System.out.println("You have selected clear, enter your manager password to clear");

                    result = scanner.next();
                    if (!Objects.equals(result, "youmustacceptthatbydoingthisyouarejepreodizingthesaveinformationofall2peoplethatwilleventuallyusethis...thinkcarefu11ybeforeyoueneter")) {
                        System.out.println("password incorrect, goodbye");
                        finished = true;
                    } else {
                        System.out.println("password accepted, clearing now");

                        String registerUrl = serverUrl + "/db";

                        var request = HttpRequest.newBuilder()
                                .uri(new URI(registerUrl))
                                .header("Authorization", userAuth)
                                .timeout(java.time.Duration.ofMillis(5000))
                                .DELETE()
                                .build();

                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    }

                }
            }
        }
    }
    public void gameMode() {
        return;
    }
}
