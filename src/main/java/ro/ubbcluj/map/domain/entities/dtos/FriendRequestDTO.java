package ro.ubbcluj.map.domain.entities.dtos;

import ro.ubbcluj.map.domain.entities.FriendRequest;

public class FriendRequestDTO {
    FriendRequest friendRequest;
    Long from;
    Long to;
    String status;

    public FriendRequestDTO(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
        from = friendRequest.getFrom().getId();
        to = friendRequest.getTo().getId();
        status = friendRequest.getStatus();
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
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
