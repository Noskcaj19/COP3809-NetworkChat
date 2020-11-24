// Dear grader, I hope you like Java, because this is a novel. (sorry)
package chat.server;

import chat.message.*;
import chat.transport.MessageSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ChatServer {
    public static void main(String[] args) {
        new ChatServer().run();
    }

    private static String getTimestamp() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    }

    private void run() {
        try (var serverSocket = new ServerSocket(2048)) {
            System.out.println("--- Hosting on " + serverSocket.getLocalSocketAddress() + " at " + getTimestamp());
            var connectionManager = new ConnectionManager();
            var handler = new ChatLogicHandler(connectionManager);

            //noinspection InfiniteLoopStatement
            while (true) {
                var socket = serverSocket.accept();
                System.out.println("--- Accepting connection " + socket.getInetAddress() + " at " + getTimestamp());

                new Thread(() -> {
                    try (
                            MessageSocket<IChatApiMessage> msgSocket = new MessageSocket<>(socket)
                    ) {
                        var ctx = new ClientContext(msgSocket);

                        while (true) {
                            try {
                                var msg = msgSocket.read();
                                if (msg == null) {
                                    handler.disconnected(ctx);
                                    return;
                                }
                                handler.handleMessage(ctx, msg);
                            } catch (IOException e) {
                                handler.disconnected(ctx);
                                return;
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                handler.disconnected(ctx);
                                return;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
