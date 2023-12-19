package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.entities.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        String erori = "";
        if(entity.getFrom() == null || entity.getTo() == null)
            throw new ValidationException("Cel putin un utilizator este inexistent!");
        if(entity.getContent().isEmpty())
            erori += "Mesaj invalid!\n";
        if(!erori.isEmpty())
            throw new ValidationException(erori);
    }
}
