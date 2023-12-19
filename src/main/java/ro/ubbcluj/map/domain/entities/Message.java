package ro.ubbcluj.map.domain.entities;

import ro.ubbcluj.map.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long>{
    private Utilizator from;
    private List<Utilizator> to;
    private String content;
    private LocalDateTime data;
    private Message replyTo;

    public Message(Utilizator from, List<Utilizator> to, String content, LocalDateTime data, Message replyTo) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.data = data;
        this.replyTo = replyTo;
    }

    public Message(Utilizator from, List<Utilizator> to, String content, Message replyTo) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.replyTo = replyTo;
        this.data = LocalDateTime.now();
    }

    public Message getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Message replyTo) {
        this.replyTo = replyTo;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if(replyTo == null)
            return from.getFirstName() + " " + from.getLastName() + "@" + data.format(Constants.DATE_TIME_FORMATTER) + ": " + content;
        else
            return from.getFirstName() + " " + from.getLastName() + "@" + data.format(Constants.DATE_TIME_FORMATTER) + "#reply to: \"" + replyTo.getContent() + "\": " + content;
    }
}
