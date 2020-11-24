package chat.server;

import chat.message.IChatApiMessage;
import chat.message.Message;
import chat.message.NewMessageEvent;
import chat.transport.MessageSocket;

import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;

public class ClientContext implements Comparable<ClientContext> {
    public final MessageSocket<IChatApiMessage> socket;
    public boolean identified = false;
    public String username;

    ClientContext(MessageSocket<IChatApiMessage> socket) {
        this.socket = socket;
    }

    public void sendMessage(Message message) throws IOException {
        socket.write(new NewMessageEvent(message));
    }

    @Override
    public int compareTo(ClientContext o) {
        return Objects.compare(username, o.username, Comparator.nullsFirst(String::compareToIgnoreCase));
    }
}
