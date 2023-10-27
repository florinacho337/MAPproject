package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.Utilizator;

import java.util.Objects;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        //TODO: implement method validate
        String erori = "";
        if (Objects.equals(entity.getLastName(), ""))
            erori += "Nume invalid!\n";
        if (Objects.equals(entity.getFirstName(), ""))
            erori += "Prenume invalid!";
        if (!Objects.equals(erori, ""))
            throw new ValidationException(erori);
    }
}
