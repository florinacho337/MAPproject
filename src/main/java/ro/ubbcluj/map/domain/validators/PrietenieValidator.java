package ro.ubbcluj.map.domain.validators;

import ro.ubbcluj.map.domain.Prietenie;

import java.util.Objects;

public class PrietenieValidator implements Validator<Prietenie> {
    @Override
    public void validate(Prietenie entity) throws ValidationException {
        String erori = "";
        if(entity.getU1() == null || entity.getU2() == null)
            erori += "Nu se poate creea o prietenie intre useri care nu exista!\n";
        if(entity.getU1() == entity.getU2())
            erori += "Nu se poate creea o prietenie cu acelasi user!";
        if(!Objects.equals(erori, ""))
            throw new ValidationException(erori);
    }
}
