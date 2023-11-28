package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.entities.Message;
import ro.ubbcluj.map.utils.exceptions.ValidationException;

import java.util.Objects;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        String erori = "";
        if(entity.getFrom() == null) {
            erori += "Emitator inexistent!";
            throw new ValidationException(erori);
        }
        final boolean[] valid = {true};
        entity.getTo().forEach(user -> {
            if(user == null)
               valid[0] = false;
        });
        if(!valid[0]) {
            erori += "Cel putin un receptor este inexistent!";
            throw new ValidationException(erori);
        }
        if(Objects.equals(entity.getMesaj(), ""))
            erori += "Mesaj inexistent!";
        throw new ValidationException(erori);
    }
}
