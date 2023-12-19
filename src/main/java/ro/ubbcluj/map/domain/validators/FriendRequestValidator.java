package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.entities.FriendRequest;

import java.util.Objects;

public class FriendRequestValidator implements Validator<FriendRequest> {
    @Override
    public void validate(FriendRequest entity) throws ValidationException {
        String erori = "";
        if(entity.getFrom() == null || entity.getTo() == null)
            throw new ValidationException("Cel putin un utilizator este inexistent!");
        if (Objects.equals(entity.getTo().getId(), entity.getFrom().getId()))
            erori += "Nu se poate da o cerere de prietenie aceluiasi utilizator!";
        if (!Objects.equals(erori, ""))
            throw new ValidationException(erori);
    }
}
