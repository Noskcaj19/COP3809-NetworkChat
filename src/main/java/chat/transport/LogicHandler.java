package chat.transport;

public interface LogicHandler<Context, IApiMessage> {
    void disconnected(Context ctx);

    void handleMessage(Context ctx, IApiMessage msg);
}
