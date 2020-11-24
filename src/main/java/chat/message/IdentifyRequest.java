package chat.message;

import java.io.Serializable;

public class IdentifyRequest implements Serializable, IChatApiMessage {
    public String username;

    public IdentifyRequest(String username) {
        this.username = username;
    }
}
