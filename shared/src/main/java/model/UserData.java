package model;
import java.util.*;



public class UserData {

    String username;
    String password;
    String email;


    public UserData(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthToken() {
        return "hi";
    }

}
