package service;
import chess.ChessGame;
import model.*;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) throws DataAccessException {
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser != null) {
            throw new DataAccessException("Error: User already exists");
        }
        if (user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        var newUser = dataAccess.addUser(user);
        return new RegisterResponse(newUser.username(), newUser.authToken());
    }

    public RegisterResponse login(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        var checkExisting = dataAccess.getUser(user.username());
        if (checkExisting == null || !BCrypt.checkpw(user.password(), checkExisting.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
        var authentication = dataAccess.addAuth(user.username());
        return new RegisterResponse(authentication.username(), authentication.authToken());
    }

    public LogoutResponse logout(String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(user);
        return new LogoutResponse();
    }

    public ListResponse list(String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        return new ListResponse(dataAccess.listGames());
    }

    public CreateResponse create(String gameName, String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null) {
            throw new DataAccessException("Error: bad request");
        }
        return new CreateResponse(dataAccess.createGame(gameName));
    }

    public JoinResponse join(JoinData game, String user) throws DataAccessException {
        var checkExistingUser = dataAccess.checkAuth(user);
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (game.gameID() == null) {
            throw new DataAccessException("Error: bad request");
        }
        AuthData thisUser = dataAccess.getUser(user, 1);
        var existingGame = dataAccess.getGame(game.gameID());
        if (existingGame == null) {
            throw new DataAccessException("Error: bad request");
        }
        String whiteUsername = null;
        String blackUsername = null;
        if (game.color() == null) {
            throw new DataAccessException("Error: bad request");
        }
        if (game.color().equals("WHITE")) {
            whiteUsername = thisUser.username();
        } else if (game.color().equals("BLACK")) {
            blackUsername = thisUser.username();
        } else if (game.color().equals("BLUE")) {
            String chosenColor = "Spectator";
        } else {
            throw new DataAccessException("Error: bad request");
        }
        if (game.color().equals("WHITE")) {
            if (existingGame.whiteUsername() != null) {
                throw new DataAccessException("Error: Forbidden");
            }
        } else if (game.color().equals("BLACK")) {
            if (existingGame.blackUsername() != null) {
                throw new DataAccessException("Error: Forbidden");
            }
        }
        var joiningGame = new GameData(game.gameID(), whiteUsername, blackUsername, game.gameName(), new ChessGame());
        dataAccess.joinGame(thisUser.username(), joiningGame);
        return new JoinResponse();
    }

    public MoveResponse move(MoveData move, String auth) throws DataAccessException {
        var checkExistingUser = dataAccess.checkAuth(auth);
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        var gameData = dataAccess.getGame(move.gameID());
        var game = gameData.game();
        try {
            game.makeMove(move.move());
            dataAccess.moveGame(game, move.gameID());
            return new MoveResponse(game);
        } catch (Exception ex) {
            throw new DataAccessException("Invalid Move");
        }
    }

    public void clear() throws DataAccessException {
        try {
            dataAccess.clear();
        }
        catch (Exception ex) {
            throw new DataAccessException("Error: Failed to clear tables");
        }
    }
}
