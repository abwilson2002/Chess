import chess.*;
import io.javalin.Javalin;
import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        Javalin.create()
                        .get("/echo/{msg}", ctx -> ctx.result("HTTP response: " + ctx.pathParam("msg")))
                                .ws("/ws", ws -> {
                                    ws.onConnect(ctx -> {
                                        ctx.enableAutomaticPings();
                                        System.out.println("Websocket connected");
                                    });
                                    ws.onMessage(ctx -> ctx.send("WebSocket response:" + ctx.message()));
                                    ws.onClose(ctx -> System.out.println("WebSocket closed"));
                                        })
                                        .start(8080);

        //server.run(8080);
        System.out.println("♕ 240 Chess Server");
    }
}