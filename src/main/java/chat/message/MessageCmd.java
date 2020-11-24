package chat.message;

import java.io.Serializable;

public class MessageCmd implements Serializable, IChatApiMessage {
    public String content;

    public MessageCmd(String content) {
        this.content = content;
    }
}
