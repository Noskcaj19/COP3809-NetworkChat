package chat.server;

import chat.message.*;
import chat.transport.LogicHandler;

import java.io.IOException;

public class ChatLogicHandler implements LogicHandler<ClientContext, IChatApiMessage> {
    private final ConnectionManager connectionManager;

    ChatLogicHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void disconnected(ClientContext ctx) {
        System.out.println("--- Disconnected: " + (ctx.identified ? ctx.username : ctx.socket));
        connectionManager.remove(ctx);
        if (ctx.identified) {
            connectionManager.broadcastMessage(new Message("system", ctx.username + " has left"));
        }
    }

    @Override
    public void handleMessage(ClientContext ctx, IChatApiMessage msg) {
        if (msg instanceof IdentifyRequest) {
            IdentifyRequest request = (IdentifyRequest) msg;
            System.out.println("--- Got identification: " + request.username);

            if (connectionManager.contains(request.username)) {
                try {
                    System.out.println("!!! Duplicate username requested: " + request.username);
                    ctx.socket.write(new NonUniqueUsernameError());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ctx.identified = true;
            ctx.username = request.username;
            connectionManager.add(ctx);
            connectionManager.broadcastMessage(new Message("system", ctx.username + " has joined"));
            try {
                ctx.sendMessage(new Message("system", "Server: " + ctx.socket.getSocket().getLocalSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!ctx.identified) {
            try {
                ctx.socket.write(new NotYetIdenfifiedError());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (msg instanceof MessageCmd) {
            MessageCmd cmd = (MessageCmd) msg;
            connectionManager.broadcastMessage(new Message(ctx.username, cmd.content));
        }
    }
}
