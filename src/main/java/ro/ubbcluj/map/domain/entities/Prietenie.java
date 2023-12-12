package ro.ubbcluj.map.domain.entities;

import ro.ubbcluj.map.utils.Constants;

import java.time.LocalDateTime;


public class Prietenie extends Entity<Tuple<String, String>> {

    LocalDateTime date;
    Utilizator u1, u2;

    public Prietenie(Utilizator u1, Utilizator u2, String date) {
        this.u1 = u1;
        this.u2 = u2;
        this.date = LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER);
    }

    public Prietenie(Utilizator u1, Utilizator u2){
        this.u1 = u1;
        this.u2 = u2;
        this.date = LocalDateTime.now();
    }

    public Utilizator getU2() {
        return u2;
    }

    public Utilizator getU1() {
        return u1;
    }

    /**
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "date=" + date.format(Constants.DATE_TIME_FORMATTER) +
                ", u1=" + u1 +
                ", u2=" + u2 +
                '}';
    }
}
