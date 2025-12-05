package client;

import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import websocket.messages.ServerMessage;

import java.lang.reflect.Type;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static client.ui.EscapeSequences.SET_BG_COLOR_BLACK;
import static client.ui.EscapeSequences.SET_TEXT_COLOR_YELLOW;
import static websocket.messages.ServerMessage.ServerMessageType.NOTIFICATION;

class MyWebSocketListener implements WebSocket.Listener {

    private final MainBackground thisInstance;
    private final StringBuilder messageBuffer = new StringBuilder();

    public MyWebSocketListener(MainBackground instance) {
        thisInstance = instance;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);
        if (last) {
            String report = messageBuffer.toString();
            messageBuffer.setLength(0);
            var gson = new Gson();
            JsonElement root = null;
            try {
                root = JsonParser.parseString(report);
            } catch (Exception ex) {
                System.out.println("failed to parse message");
            }

            String commandType = root.getAsJsonObject().get("serverMessageType").getAsString();

            ServerMessage.ServerMessageType mType = ServerMessage.ServerMessageType.valueOf(commandType);

            switch (mType) {
                case LOAD_GAME -> {
                    Type type = new TypeToken<Map<String, ChessPiece>>() {
                    }.getType();

                    JsonObject allPiecesMap = root.getAsJsonObject().getAsJsonObject("game");

                    Map<String, ChessPiece> progress = gson.fromJson(allPiecesMap, type);

                    this.thisInstance.boardPrinterHighlight(progress, false, null);
                }
                case ERROR, NOTIFICATION -> {
                    String message = "";
                    if (mType == NOTIFICATION) {
                        message = root.getAsJsonObject().get("message").getAsString();
                    } else {
                        message = root.getAsJsonObject().get("errorMessage").getAsString();
                    }

                    System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_YELLOW + message);
                }
                case LOAD_HIGHLIGHT -> {
                    Type type = new TypeToken<Map<String, ChessPiece>>() {
                    }.getType();

                    JsonObject board = root.getAsJsonObject().getAsJsonObject("game");

                    Map<String, ChessPiece> progress = gson.fromJson(board, type);

                    type = new TypeToken<Collection<ChessMove>>() {
                    }.getType();

                    var vMoves = root.getAsJsonObject().get("moves");

                    Collection<ChessMove> moves = gson.fromJson(vMoves, type);

                    this.thisInstance.boardPrinterHighlight(progress, true, moves);
                }
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    public void onClose(WebSocket webSocket) {
        WebSocket.Listener.super.onClose(webSocket, 200, "Done");
    }

    public void onError(WebSocket webSocket, Exception ex) {
        WebSocket.Listener.super.onError(webSocket, ex);
    }
}
