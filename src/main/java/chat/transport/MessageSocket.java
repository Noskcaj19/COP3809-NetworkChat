package chat.transport;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageSocket<IApiMessage> implements Closeable {
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Socket socket;

    public MessageSocket(Socket socket) throws IOException {
        this.socket = socket;
        // Output must come first or it hangs (why?)
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    @SuppressWarnings("unchecked") // yuck
    public IApiMessage read() throws IOException, ClassNotFoundException, ClassCastException {
        var obj = inputStream.readObject();

        return (IApiMessage) obj;
    }

    public void write(IApiMessage obj) throws IOException {
        outputStream.writeObject(obj);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        System.out.println("--- MsgSocket closed");
    }

    public Socket getSocket() {
        return socket;
    }
}
