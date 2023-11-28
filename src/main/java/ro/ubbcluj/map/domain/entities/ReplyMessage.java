package ro.ubbcluj.map.domain.entities;

import java.util.List;

public class ReplyMessage extends Message{
    private Message message;

    public ReplyMessage(Utilizator from, List<Utilizator> to, String mesaj, Message message) {
        super(from, to, mesaj);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
