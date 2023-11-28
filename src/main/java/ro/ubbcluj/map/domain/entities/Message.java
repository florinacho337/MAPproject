package ro.ubbcluj.map.domain.entities;

import ro.ubbcluj.map.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long>{
    private Utilizator from;
    private List<Utilizator> to;
    private String mesaj;
    private LocalDateTime data;

    public Message(Utilizator from, List<Utilizator> to, String mesaj) {
        this.from = from;
        this.to = to;
        this.mesaj = mesaj;
        this.data = LocalDateTime.now();
    }

    public Utilizator getFrom() {
        return from;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public List<Utilizator> getTo() {
        return to;
    }

    public void setTo(List<Utilizator> to) {
        this.to = to;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMessage(String message) {
        this.mesaj = message;
    }

    public LocalDateTime getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", mesaj='" + mesaj + '\'' +
                ", data=" + data.format(Constants.DATE_TIME_FORMATTER) +
                '}';
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
