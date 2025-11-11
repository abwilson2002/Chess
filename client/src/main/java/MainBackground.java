
import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import org.eclipse.jetty.websocket.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class MainBackground {

    private static final Logger log = LoggerFactory.getLogger(MainBackground.class);
    private String user = null;
    boolean signedIn = false;
    private String userAuth = "";
    private HttpClient httpClient = HttpClient.newHttpClient();
    private String serverUrl;

    public MainBackground(String serverName) throws Exception {

        serverUrl = serverName;

    }

    public void running() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("♕ 240 Chess Client\n");
        System.out.println("What is your command?");

        Gson gson = new Gson();



        boolean finished = false;
        while (!finished) {
            var scanner = new Scanner(System.in);
            var result = scanner.nextLine();

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
                case ("register") -> {
                    System.out.println("Username, password, email");

                    result = scanner.nextLine();
                    String username = result;
                    result = scanner.nextLine();
                    String pass = result;
                    result = scanner.nextLine();
                    String email = result;

                    var input = Map.of("username", username, "password", pass, "email", email);
                    var requestInput = gson.toJson(input);

                    String registerUrl = serverUrl + "/user";

                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    String responseBody = response.body();

                    AuthData output = gson.fromJson(responseBody, AuthData.class);

                    userAuth = output.authToken();
                    user = output.username();


                    System.out.println("Registered as: " + user);
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
                case ("help") -> {

                }
                case ("clear") -> {
                    System.out.println("You have selected clear, enter your manager password to clear");

                    result = scanner.nextLine();
                    if (result != "youmustacceptthatbydoingthisyouarejepreodizingthesaveinformationofall2peoplethatwilleventuallyusethis...thinkcarefu11ybeforeyoueneter") {
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
                    }

                }
            }
        }


    }

}
