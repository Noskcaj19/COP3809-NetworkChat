package event;

import java.util.EventListener;

@FunctionalInterface
public interface Subscriber<T> extends EventListener {
    void handle(T data);
}
