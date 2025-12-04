package model;

import websocket.messages.ServerMessage;

public record NotifGameResponse(ServerMessage type, String message) {
}
