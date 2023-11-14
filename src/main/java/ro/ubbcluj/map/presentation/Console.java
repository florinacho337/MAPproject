package ro.ubbcluj.map.presentation;

import ro.ubbcluj.map.domain.Prietenie;
import ro.ubbcluj.map.domain.Tuple;
import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.domain.validators.ValidationException;
import ro.ubbcluj.map.repository.FriendshipDBRepository;
import ro.ubbcluj.map.repository.UserDBRepository;
import ro.ubbcluj.map.service.DuplicateException;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;

import java.util.*;
import java.util.stream.StreamSupport;

public class Console {
    private final FriendshipsService friendshipsService;
//    UsersService usersService;
    private final UsersService usersService;
    private static final Console instance = new Console();

    private Console() {
        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password = "postgres";
//        UtilizatorValidator validatorUser = new UtilizatorValidator();
//        PrietenieValidator validatorFriendship = new PrietenieValidator();
//        InMemoryRepository<Long, Utilizator> repoUsers = new InMemoryRepository<>(validatorUser);
//        InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships = new InMemoryRepository<>(validatorFriendship);
        UserDBRepository repoUsers = new UserDBRepository(url, username, password);
        FriendshipDBRepository repoFriendships = new FriendshipDBRepository(url, username, password);
        usersService = new UsersService(repoUsers);
        friendshipsService = new FriendshipsService(repoFriendships, repoUsers);
    }

    public static Console getInstance() {
        return instance;
    }

    private static void afiseazaPrieteniiLuna(Tuple<Utilizator, String> prietenie) {
        String firstName = prietenie.getLeft().getFirstName();
        String lastName = prietenie.getLeft().getLastName();
        String friendsFrom = prietenie.getRight();
        System.out.format("%s| %s| %s\n", firstName, lastName, friendsFrom);
    }

    private void Meniu() {
        System.out.println("============================MENIU==================================");
        System.out.println("|  0.Iesi din program(exit)                                       |");
        System.out.println("|  1.Adauga user (add_user firstName lastName)                    |");
        System.out.println("|  2.Sterge user (rm_user id) - NOT FUNCTIONAL                    |");
        System.out.println("|  3.Afiseaza useri (show_users)                                  |");
        System.out.println("|  4.Adauga prietenie (add_friendship idU1 idU2)                  |");
        System.out.println("|  5.Sterge prietenie (rm_friendship idU1 idU2) - NOT FUNCTIONAL  |");
        System.out.println("|  6.Afiseaza prietenii (show_friendships)                        |");
        System.out.println("|  7.Afiseaza  numarul de comunitati (nr_comunitati)              |");
        System.out.println("|  8.Afiseaza cea mai sociabia comunitate (comunitate_sociabila)  |");
        System.out.println("|  9.Afiseaza prieteniile unui utilizator dintr-o luna            |");
        System.out.println("|    (prietenii_luna id luna)                                     |");
        System.out.println("|  Pentru a afisa meniul ulterior tastati \"meniu\".                |");
        System.out.println("===================================================================");
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
                    case "show_users":
                        afiseazaUseri(parts);
                        break;
                    case "show_friendships":
                        afiseazaPrietenii(parts);
                        break;
                    case "prietenii_luna":
                        prieteniiLuna(parts);
                        break;
                    default:
                        System.out.println("Comanda invalida!");
                }
            } catch (IllegalArgumentException | DuplicateException | ValidationException | NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void prieteniiLuna(String[] parts) {
        if(parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        List<Tuple<Utilizator, String>> prietenii = friendshipsService.prieteniiDinLuna(Long.parseLong(parts[1]),
                Integer.parseInt(parts[2]));
        Utilizator utilizator = usersService.find(Long.parseLong(parts[1]));
        if(prietenii.isEmpty())
            System.out.format("Nu exista prietenii ale utilizatorului " + utilizator + " in luna %s.\n", parts[2]);
        prietenii.forEach(Console::afiseazaPrieteniiLuna);
    }

    private void afiseazaPrietenii(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        Iterable<Prietenie> prietenii = friendshipsService.getAll();
        if (StreamSupport.stream(prietenii.spliterator(), false).findAny().isEmpty()) {
            System.out.println("Nu exista prietenii!");
            return;
        }
        prietenii.forEach(prietenie -> System.out.format("(%s). %s\n", prietenie.getId(), prietenie));
    }

    private void afiseazaUseri(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        Iterable<Utilizator> users = usersService.getAll();
        if (StreamSupport.stream(users.spliterator(), false).findAny().isEmpty()) {
            System.out.println("Nu exista utilizatori!");
            return;
        }
        users.forEach(user -> System.out.format("%d. %s\n", user.getId(), user));
    }

    private void comunitateSociabila(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        ArrayList<Integer> list = friendshipsService.biggestConnectedComponent();
        list.forEach(l ->{
            Utilizator user = usersService.find(Long.valueOf(l));
            System.out.format("%d. %s\n", user.getId(), user);
        });
    }

    private void nrComunitati(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
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
        if (parts.length != 4) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator u1 = usersService.find(Long.valueOf(parts[1]));
        Utilizator u2 = usersService.find(Long.valueOf(parts[2]));
        Prietenie prietenie = new Prietenie(u1, u2, parts[3]);
        friendshipsService.add(prietenie);
        System.out.println(prietenie + " cu id-ul " + prietenie.getId() + " adaugata cu succes!");
    }

    private void removeUser(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        friendshipsService.removePrietenii(Long.parseLong(parts[1]));
        Utilizator user_sters = usersService.remove(Long.parseLong(parts[1]));
        System.out.println(user_sters + " si prieteniile acestuia au fost sterse cu succes!");
    }

}
