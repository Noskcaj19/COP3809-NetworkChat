https://github.com/Noskcaj19/COP3809-NetworkChat

The client code is in chat.client.ChatClient
The server code is in chat.server.ChatServer
The actual server logic is in chat.server.ChatLogicHandler

The chat.transport package provides a wrapper class around a socket to pass typed messages
the chat.message package contains the typed messages that are sent between the client and server
the event package is a simple event bus implementation

Compile with:
javac chat/client/ChatClient.java chat/server/ChatServer.java --module-path '<path-to-javafx>/lib' --add-modules javafx.controls

Run with:
java --module-path '<path-to-javafx>/lib' --add-modules javafx.controls chat.client.ChatClient
java chat.server.ChatServer
