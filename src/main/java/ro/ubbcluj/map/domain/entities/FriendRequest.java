package ro.ubbcluj.map.domain.entities;

public class FriendRequest extends Entity<Long>{
    private Utilizator from;
    private Utilizator to;
    private String status;

    public FriendRequest(Utilizator from, Utilizator to) {
        this.from = from;
        this.to = to;
        this.status = "pending";
    }

    public FriendRequest(Utilizator from, Utilizator to, String status) {
        this.from = from;
        this.to = to;
        this.status = status;
    }

    public Utilizator getFrom() {
        return from;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public Utilizator getTo() {
        return to;
    }

    public void setTo(Utilizator to) {
        this.to = to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", status='" + status + '\'' +
                '}';
    }
}
