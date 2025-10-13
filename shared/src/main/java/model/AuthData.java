package model;

public class AuthData {
    String authToken;
    String username;


    public AuthData(String authToken, String username) {
        this.username = username;
        this.authToken = authToken;
    }
}
