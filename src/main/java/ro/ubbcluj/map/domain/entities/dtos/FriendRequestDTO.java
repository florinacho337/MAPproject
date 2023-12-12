package ro.ubbcluj.map.domain.entities.dtos;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Utilizator;

public class FriendRequestDTO {
    private final FriendRequest friendRequest;
    private String from;
    private final String firstNameFrom;
    private final String lastNameFrom;
    private String to;
    private String status;

    public FriendRequestDTO(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
        from = friendRequest.getFrom().getId();
        to = friendRequest.getTo().getId();
        status = friendRequest.getStatus();
        firstNameFrom = friendRequest.getFrom().getFirstName();
        lastNameFrom = friendRequest.getFrom().getLastName();
    }

    public String getFirstNameFrom(){return firstNameFrom;}
    public String getLastNameFrom(){return lastNameFrom;}

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FriendRequest getFriendRequest() {
        return friendRequest;
    }
}
