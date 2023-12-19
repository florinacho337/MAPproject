package ro.ubbcluj.map.domain.entities.dtos;

import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.utils.Constants;

import java.util.Objects;

public class PrietenDTO {
    Prietenie prietenie;
    Utilizator user;
    Utilizator prieten;
    String id;
    String firstName;
    String lastName;
    String friendsFrom;


    public Utilizator getPrieten() {
        return prieten;
    }

    public PrietenDTO(Prietenie prietenie, Utilizator utilizator) {
        this.prietenie = prietenie;
        this.user = utilizator;
        if(Objects.equals(utilizator.getId(), prietenie.getU1().getId())){
            id = prietenie.getU2().getId();
            prieten = prietenie.getU2();
            firstName = prietenie.getU2().getFirstName();
            lastName = prietenie.getU2().getLastName();
        } else{
            id = prietenie.getU1().getId();
            prieten = prietenie.getU1();
            firstName = prietenie.getU1().getFirstName();
            lastName = prietenie.getU1().getLastName();
        }
        friendsFrom = prietenie.getDate().format(Constants.DATE_TIME_FORMATTER);
    }

    public Prietenie getPrietenie() {
        return prietenie;
    }

    public Utilizator getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFriendsFrom() {
        return friendsFrom;
    }
}
