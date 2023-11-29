package ro.ubbcluj.map.utils.events;

import ro.ubbcluj.map.domain.entities.Utilizator;

public class UtilizatorChangeEvent implements Event {
    private final ChangeEventType type;
    private final Utilizator data;
    private Utilizator oldData;

    public UtilizatorChangeEvent(ChangeEventType type, Utilizator data) {
        this.type = type;
        this.data = data;
    }
    public UtilizatorChangeEvent(ChangeEventType type, Utilizator data, Utilizator oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Utilizator getData() {
        return data;
    }

    public Utilizator getOldData() {
        return oldData;
    }
}