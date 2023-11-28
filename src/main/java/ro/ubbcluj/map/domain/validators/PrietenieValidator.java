package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.utils.exceptions.ValidationException;

import java.util.Objects;

public class PrietenieValidator implements Validator<Prietenie> {
    @Override
    public void validate(Prietenie entity) throws ValidationException {
        String erori = "";
        if(entity.getU1() == null || entity.getU2() == null)
            throw new ValidationException("Cel putin un utilizator este inexistent!");
        if (Objects.equals(entity.getU1().getId(), entity.getU2().getId()))
            erori += "Nu se poate creea o prietenie cu acelasi user!";
        if (!Objects.equals(erori, ""))
            throw new ValidationException(erori);
    }
}
