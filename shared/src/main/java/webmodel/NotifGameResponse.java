package webmodel;

import websocket.messages.ServerMessage;

public record NotifGameResponse(ServerMessage.ServerMessageType serverMessageType, String message, String errorMessage) {
}
