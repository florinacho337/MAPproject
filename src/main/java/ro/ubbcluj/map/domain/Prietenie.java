package ro.ubbcluj.map.domain;

import ro.ubbcluj.map.utils.Constants;

import java.time.LocalDate;


public class Prietenie extends Entity<Tuple<Long, Long>> {

    LocalDate date;
    Utilizator u1, u2;

    public Prietenie(Utilizator u1, Utilizator u2, String date) {
        this.u1 = u1;
        this.u2 = u2;
        this.date = LocalDate.parse(date, Constants.DATE_TIME_FORMATTER);
    }

    public Prietenie(Utilizator u1, Utilizator u2){
        this.u1 = u1;
        this.u2 = u2;
        this.date = LocalDate.now();
        this.date.format(Constants.DATE_TIME_FORMATTER);
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
    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "date=" + date +
                ", u1=" + u1 +
                ", u2=" + u2 +
                '}';
    }
}
