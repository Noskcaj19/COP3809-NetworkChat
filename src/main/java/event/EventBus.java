package event;

import java.util.ArrayList;

public class EventBus<T> {
    private final ArrayList<Subscriber<T>> subscriptions = new ArrayList<>();

    public void subscribe(Subscriber<T> subscriber) {
        subscriptions.add(subscriber);
    }

    public void emit(T data) {
        subscriptions.forEach(subscriber -> subscriber.handle(data));
    }
}
