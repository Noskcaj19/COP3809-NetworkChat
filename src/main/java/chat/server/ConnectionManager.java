package chat.server;

import chat.message.Message;
import chat.message.NewMessageEvent;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConnectionManager {
    private final ConcurrentSkipListSet<ClientContext> connections = new ConcurrentSkipListSet<>();

    public boolean contains(String username) {
        return connections.stream().filter(ctx -> ctx.identified).anyMatch(ctx -> ctx.username.equals(username));
    }

    public void add(ClientContext ctx) {
        connections.add(ctx);
    }

    public void remove(ClientContext ctx) {
        connections.remove(ctx);
    }

    public void broadcastMessage(Message message) {
        connections.forEach(otherCtx -> {
            try {
                otherCtx.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
