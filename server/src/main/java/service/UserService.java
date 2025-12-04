package service;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.*;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;
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
        var user = dataAccess.getUser(auth, 1);
        var gameData = dataAccess.getGame(move.gameID());
        var game = gameData.game();
        if (game.gameOver) {
            throw new DataAccessException("Game is over, no moves allowed");
        }
        boolean whitePlayer = !Objects.equals(gameData.blackUsername(), user.username());
        if (((game.getTeamTurn() == ChessGame.TeamColor.WHITE) & !whitePlayer) || (game.getTeamTurn() == ChessGame.TeamColor.BLACK) & whitePlayer) {
            throw new DataAccessException("Not your turn");
        }
        if (((game.getBoard().getPiece(move.move().getStartPosition()).getTeamColor() == ChessGame.TeamColor.WHITE) & !whitePlayer)
                || (game.getBoard().getPiece(move.move().getStartPosition()).getTeamColor() == ChessGame.TeamColor.BLACK) & whitePlayer) {
            throw new DataAccessException("Not your piece");
        }
        try {
            game.makeMove(move.move());
            dataAccess.moveGame(game, move.gameID());
            String gameState = "";
            ChessGame.TeamColor enemyColor = (whitePlayer) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            if (game.isInStalemate(enemyColor)) {
                gameState = "Stalemate";
            } else if (game.isInCheck(enemyColor)) {
                if (game.isInCheckmate(enemyColor)) {
                    gameState = "Checkmate";
                } else {
                    gameState = "Check";
                }
            }
            return new MoveResponse(game.getBoard().getAllPieces(), user.username(), gameState);
        } catch (Exception ex) {
            throw new DataAccessException("Invalid Move");
        }
    }

    public LoadResponse load(LoadGameData game) throws DataAccessException {
        var checkExistingUser = dataAccess.checkAuth(game.auth());
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        var gameData = dataAccess.getGame(game.gameID());
        var gameInstance = gameData.game();
        return new LoadResponse(gameInstance.getBoard().getAllPieces());
    }

    public LeaveResponse leave(LeaveGameData data) throws DataAccessException {
        if (Objects.equals(data.auth(), "observer")) {
            return new LeaveResponse("observer", "An observer has left the game");
        }
        var checkExistingUser = dataAccess.checkAuth(data.auth());
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        var user = dataAccess.getUser(data.auth(), 1).username();
        var message = "";
        try {
            dataAccess.dropPlayer(user, data.gameID());
            var game = dataAccess.getGame(data.gameID()).game();
            if (game.gameOver) {
                message = user + " has left the game";
            } else {
                message = user + " has taken the cowards way out and left";
            }
        } catch (Exception ex) {
            throw new DataAccessException("Error: could not leave");
        }
        return new LeaveResponse(user, message);
    }

    public HighlightResponse highlight(HighGameData data) throws DataAccessException{
        var checkExistingUser = dataAccess.checkAuth(data.auth());
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        try {
            var game = dataAccess.getGame(data.gameID());
            var stringPosition = data.position();
            var positionCheck = new ChessPosition((stringPosition.charAt(1) - '0'), (stringPosition.charAt(0) - '0'));
            Collection<ChessMove> moves = game.game().validMoves(positionCheck);
            return new HighlightResponse(game.game().getBoard().getAllPieces(), moves);
        } catch (Exception ex) {
            throw new DataAccessException("Error: failed to highlight");
        }
    }

    public void resign(String auth, Double gameID) throws DataAccessException {
        var checkExistingUser = dataAccess.checkAuth(auth);
        if (!checkExistingUser) {
            throw new DataAccessException("Error: unauthorized");
        }
        var user = dataAccess.getUser(auth, 1);
        var game = dataAccess.getGame(gameID);
        if (!Objects.equals(user.username(), game.whiteUsername()) || !Objects.equals(user.username(), game.blackUsername())) {
            throw new DataAccessException("Error: not allowed to resign if you are not a player");
        }
        game.game().gameOver = true;
        dataAccess.moveGame(game.game(), gameID);
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
