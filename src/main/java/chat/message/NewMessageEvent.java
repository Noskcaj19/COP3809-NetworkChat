package chat.message;

import java.io.Serializable;

public class NewMessageEvent implements Serializable, IChatApiMessage {
    public final Message message;

    public NewMessageEvent(Message message) {
        this.message = message;
    }

    public NewMessageEvent(String author, String content) {
        this.message = new Message(author, content);
    }
}
