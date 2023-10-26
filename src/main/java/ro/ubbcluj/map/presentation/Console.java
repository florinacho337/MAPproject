package ro.ubbcluj.map.presentation;

import ro.ubbcluj.map.domain.Prietenie;
import ro.ubbcluj.map.domain.Tuple;
import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.domain.validators.PrietenieValidator;
import ro.ubbcluj.map.domain.validators.UtilizatorValidator;
import ro.ubbcluj.map.domain.validators.ValidationException;
import ro.ubbcluj.map.repository.InMemoryRepository;
import ro.ubbcluj.map.service.DuplicateException;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Console {
    FriendshipsService friendshipsService;
    UsersService usersService;
    private static final Console instance = new Console();

    private Console() {
        UtilizatorValidator validatorUser = new UtilizatorValidator();
        PrietenieValidator validatorFriendship = new PrietenieValidator();
        InMemoryRepository<Long, Utilizator> repoUsers = new InMemoryRepository<>(validatorUser);
        InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships = new InMemoryRepository<>(validatorFriendship);
        usersService = new UsersService(repoUsers);
        friendshipsService = new FriendshipsService(repoFriendships, repoUsers);
    }

    public static Console getInstance() {
        return instance;
    }

    private void Meniu() {
        System.out.println("0.Iesi din program(exit)\n");
        System.out.println("1.Adauga user (add_user firstName lastName)");
        System.out.println("2.Sterge user (rm_user id)");
        System.out.println("3.Adauga prietenie (add_friendship idU1 idU2)");
        System.out.println("4.Sterge prietenie (rm_friendship idU1 idU2)");
        System.out.println("5.Afiseaza  numarul de comunitati (nr_comunitati)");
        System.out.println("6.Afiseaza cea mai sociabia comunitate (comunitate_sociabila)");
        System.out.println("Pentru a afisa meniul ulterior tastati \"meniu\". ");
    }

    public void run() {

        Meniu();
        while (true) {
            Scanner s = new Scanner(System.in);
            String comanda = s.nextLine();
            String[] parts = comanda.split(" ");

            if (Objects.equals(parts[0], " ")) continue;

            try {
                switch (parts[0]) {
                    case "exit":
                        return;
                    case "meniu":
                        Meniu();
                        break;
                    case "add_user":
                        addUser(parts);
                        break;
                    case "rm_user":
                        removeUser(parts);
                        break;
                    case "add_friendship":
                        addFriendship(parts);
                        break;
                    case "rm_friendship":
                        removeFriendhsip(parts);
                        break;
                    case "nr_comunitati":
                        nrComunitati(parts);
                        break;
                    case "comunitate_sociabila":
                        comunitateSociabila(parts);
                        break;
                    default:
                        System.out.println("Comanda invalida!");
                }
            } catch (IllegalArgumentException | DuplicateException | ValidationException e){
                System.out.println(e.getMessage());
            }
        }

    }

    private void comunitateSociabila(String[] parts) {
        if(parts.length != 1){
            System.out.println("NUmar de parametrii invalid!");
            return;
        }
        ArrayList<Integer> list = friendshipsService.biggestConnectedComponent();
        for(int l:list){
            System.out.println(usersService.find((long) l));
        }
    }

    private void nrComunitati(String[] parts) {
        if(parts.length != 1){
            System.out.println("NUmar de parametrii invalid!");
            return;
        }
        System.out.println("Numarul de comunitati din retea este: " + friendshipsService.connectedComponents());
    }

    private void removeFriendhsip(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Tuple<Long, Long> id = new Tuple<>(Long.valueOf(parts[1]), Long.valueOf(parts[2]));
        Prietenie prietenie = friendshipsService.remove(id);
        if(prietenie == null){
            System.out.println("Acesasta prietenie nu exista!");
            return;
        }
        System.out.println(prietenie + " stearsa cu succes!");
    }

    private void addUser(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator utilizator = new Utilizator(parts[1], parts[2]);
        usersService.add(utilizator);
        System.out.println(utilizator + " cu id-ul " + utilizator.getId() + " a fost adaugat cu succes!");
    }

    private void addFriendship(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator u1 = usersService.find(Long.valueOf(parts[1]));
        Utilizator u2 = usersService.find(Long.valueOf(parts[2]));
        Prietenie prietenie = new Prietenie(u1, u2);
        friendshipsService.add(prietenie);
        System.out.println(prietenie + " cu id-ul " + prietenie.getId() + " adaugata cu succes!");
    }

    private void removeUser(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator user_sters = usersService.remove(Long.parseLong(parts[1]));
        if(user_sters == null) {
            System.out.println("Acest user nu exista!");
            return;
        }
        friendshipsService.removePrietenii(user_sters);
        System.out.println(user_sters + " si prieteniile acestuia au fost sterse cu succes!");
    }

}
