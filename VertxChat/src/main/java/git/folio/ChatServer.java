package git.folio;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ChatServer extends AbstractVerticle {

    private Map<String, ServerWebSocket> connectedClients = new HashMap<>();

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        server.webSocketHandler(websocket -> {
            String clientId = websocket.textHandlerID();
            connectedClients.put(clientId, websocket);

            websocket.textMessageHandler(message -> {
                JsonObject jsonMessage = new JsonObject(message);
                String username = jsonMessage.getString("username");
                String text = jsonMessage.getString("text");
                broadcast(username, text);
            });

            websocket.closeHandler(v -> {
                connectedClients.remove(clientId);
                System.out.println("Client disconnected: " + clientId);
            });

            System.out.println("New client connected: " + clientId);
        });

        server.listen(8010, result -> {
            if (result.succeeded()) {
                System.out.println("Chat server is listening on port 8010");
            } else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }

    private void broadcast(String username, String message) {
        JsonObject broadcastMessage = new JsonObject()
                .put("username", username)
                .put("text", message);

        for (ServerWebSocket client : connectedClients.values()) {
            client.writeTextMessage(broadcastMessage.encode());
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ChatServer());
    }
}