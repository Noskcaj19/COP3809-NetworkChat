module chat {
    requires javafx.controls;
    exports chat.client;
    exports chat.server;
    exports chat.message;
    exports chat.transport;
}