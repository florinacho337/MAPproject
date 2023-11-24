package ro.ubbcluj.map.utils.observer;

import ro.ubbcluj.map.utils.events.Event;
public interface Observer<E extends Event> {
    void update(E e);
}