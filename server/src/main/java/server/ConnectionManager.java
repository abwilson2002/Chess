package server;

import org.eclipse.jetty.websocket.api.Session;
import io.javalin.websocket.WsSession;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionManager {

    private final Map<Double, Set<Session>> connections = new HashMap<>();

    public void addConnection(double gameID, Session session) {
        Set<Session> gameSessions = connections.computeIfAbsent(gameID, k -> new CopyOnWriteArraySet<>());
        gameSessions.add(session);
        connections.put(gameID, gameSessions);
    }

    public void removeConnection(Session session) {
        for (Set<Session> gameSessions : connections.values()) {
            gameSessions.remove(session);
        }
    }

    public Collection<Session> getAllSessions(double gameID) {
        return connections.get(gameID);
    }






}
