package chat.message;

import java.io.Serializable;

public class Message implements Serializable  {
    public final String author;
    public final String content;

    public Message(String author, String content) {
        this.author = author;
        this.content = content;
    }
}