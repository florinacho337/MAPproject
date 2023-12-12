package ro.ubbcluj.map.presentation;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Tuple;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.repository.dbrepositories.FriendRequestDBRepo;
import ro.ubbcluj.map.repository.dbrepositories.FriendshipDBRepository;
import ro.ubbcluj.map.repository.dbrepositories.MessageDBRepository;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.service.FriendshipsService;
import ro.ubbcluj.map.service.UsersService;
import ro.ubbcluj.map.utils.exceptions.DuplicateException;
import ro.ubbcluj.map.utils.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.StreamSupport;

public class Console {
    private final FriendshipsService friendshipsService;
    private final UsersService usersService;
    private static final Console instance = new Console();

    private Console() {
        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password = "postgres";
//        UtilizatorValidator validatorUser = new UtilizatorValidator();
//        PrietenieValidator validatorFriendship = new PrietenieValidator();
//        FriendRequestValidator friendRequestValidator = new FriendRequestValidator();
//        MessageValidator messageValidator = new MessageValidator();
//        InMemoryRepository<Long, Utilizator> repoUsers = new InMemoryRepository<>(validatorUser);
//        InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships = new InMemoryRepository<>(validatorFriendship);
//        InMemoryRepository<Long, FriendRequest> repoFriendRequest = new InMemoryRepository<>(friendRequestValidator);
//        InMemoryRepository<Long, Message> repoMessages = new InMemoryRepository<>(messageValidator);
        UserDBRepository repoUsers = new UserDBRepository(url, username, password);
        FriendshipDBRepository repoFriendships = new FriendshipDBRepository(url, username, password);
        FriendRequestDBRepo repoFriendRequest = new FriendRequestDBRepo(url, username, password);
        MessageDBRepository repoMessages = new MessageDBRepository(url, username, password);
        usersService = new UsersService(repoUsers, repoMessages);
        friendshipsService = new FriendshipsService(repoFriendships, repoUsers, repoFriendRequest);
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
        System.out.println("|  2.Sterge user (rm_user id)                                     |");
        System.out.println("|  3.Afiseaza useri (show_users)                                  |");
        System.out.println("|  4.Actualizeaza user (update_user id newFirstName newLastName)  |");
        System.out.println("|  5.Adauga prietenie (add_friendship idU1 idU2)                  |");
        System.out.println("|  6.Sterge prietenie (rm_friendship idU1 idU2)                   |");
        System.out.println("|  7.Afiseaza prietenii (show_friendships)                        |");
        System.out.println("|  8.Afiseaza  numarul de comunitati (nr_comunitati)              |");
        System.out.println("|  9.Afiseaza cea mai sociabia comunitate (comunitate_sociabila)  |");
        System.out.println("| 10.Afiseaza prieteniile unui utilizator dintr-o luna            |");
        System.out.println("|    (prietenii_luna id luna)                                     |");
        System.out.println("| 11.Afiseaza cereri de prietenie (show_friend_requests)          |");
        System.out.println("| 12.Trimite cerere de prietenie (friend_request id_from id_to)   |");
        System.out.println("| 13.Accepta cerere de prietenie                                  |");
        System.out.println("|    (accept_friend_request idFriendRequest)                      |");
        System.out.println("| 14.Refuza cerere de prietenie                                   |");
        System.out.println("|    (reject_friend_request idFriendRequest)                      |");
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
                    case "update_user":
                        updateUser(parts);
                        break;
                    case "friend_request":
                        friendRequest(parts);
                        break;
                    case "show_friend_requests":
                        showFriendRequests(parts);
                        break;
                    case "accept_friend_request":
                        acceptFriendRequest(parts);
                        break;
                    case "reject_friend_request":
                        rejectFriendRequest(parts);
                        break;
                    default:
                        System.out.println("Comanda invalida!");
                }
            } catch (IllegalArgumentException | ValidationException | DuplicateException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void rejectFriendRequest(String[] parts) {
        if(parts.length != 2) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        FriendRequest friendRequest = friendshipsService.findFriendRequest(Long.valueOf(parts[1]));
        if(Objects.equals(friendRequest.getStatus(), "approved") || Objects.equals(friendRequest.getStatus(), "rejected")) {
            System.out.println("Cererea de prietenie a fost deja acceptata sau refuzata!");
            return;
        }
        friendRequest = friendshipsService.rejectFriendRequest(friendRequest);
        if(friendRequest == null)
            System.out.println("Cerere de prietenie inexistenta!");
        else
            System.out.println("Cerere de prietenie refuzata!");
    }

    private void acceptFriendRequest(String[] parts) {
        if(parts.length != 2) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        FriendRequest friendRequest = friendshipsService.findFriendRequest(Long.valueOf(parts[1]));
        if(Objects.equals(friendRequest.getStatus(), "approved") || Objects.equals(friendRequest.getStatus(), "rejected")) {
            System.out.println("Cererea de prietenie a fost deja acceptata sau refuzata!");
            return;
        }
        friendRequest = friendshipsService.acceptFriendRequest(friendRequest);
        if(friendRequest == null)
            System.out.println("Cerere de prietenie inexistenta!");
        else
            System.out.println("Cerere de prietenie acceptata!");
    }

    private void showFriendRequests(String[] parts) {
        if(parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        Iterable<FriendRequest> friendRequests = friendshipsService.getFriendRequests();
        if (StreamSupport.stream(friendRequests.spliterator(), false).findAny().isEmpty()) {
            System.out.println("Nu exista cereri de prietenie!");
            return;
        }
        friendRequests.forEach(System.out::println);
    }

    private void friendRequest(String[] parts) {
        if(parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        Utilizator from = usersService.find(parts[1]);
        Utilizator to = usersService.find(parts[2]);
        if(friendshipsService.sendFriendRequest(from, to) == null)
            System.out.println("Cerere de prietenie trimisa cu succes!");
        else
            System.out.println("Cerere de prietenie existenta!");
    }

    private void updateUser(String[] parts) {
        if(parts.length != 5) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        Utilizator utilizator = new Utilizator(parts[2], parts[3], parts[1], parts[4]);
        utilizator.setId(parts[1]);
        if(usersService.update(utilizator) == null)
            System.out.println("Utilizator actualizat cu succes!");
        else
            System.out.println("Nu s-a putut actualiza utilizatorul!");
    }

    private void prieteniiLuna(String[] parts) {
        if(parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        List<Tuple<Utilizator, String>> prietenii = friendshipsService.prieteniiDinLuna(parts[1],
                Integer.parseInt(parts[2]));
        Utilizator utilizator = usersService.find(parts[1]);
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
        users.forEach(user -> System.out.format("%s. %s\n", user.getId(), user));
    }

    private void comunitateSociabila(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }
        ArrayList<String> list = friendshipsService.biggestConnectedComponent();
        list.forEach(l ->{
            Utilizator user = usersService.find(l);
            System.out.format("%s. %s\n", user.getId(), user);
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

        Tuple<String, String> id = new Tuple<>(parts[1], parts[2]);
        Prietenie prietenie = friendshipsService.remove(id);
        if(prietenie != null)
            System.out.println(prietenie + " stearsa cu succes!");
        else
            System.out.println("Nu s-a putut sterge prietenia!");
    }

    private void addUser(String[] parts) {
        if (parts.length != 5) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator utilizator = new Utilizator(parts[1], parts[2], parts[3], parts[4]);
        if(usersService.add(utilizator) == null)
            System.out.println(utilizator + " a fost adaugat cu succes!");
        else
            System.out.println("Utilizatorul este deja existent!");
    }

    private void addFriendship(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator u1 = usersService.find(parts[1]);
        Utilizator u2 = usersService.find(parts[2]);
        Prietenie prietenie = new Prietenie(u1, u2);
        friendshipsService.add(prietenie);
        System.out.println(prietenie + " cu id-ul " + prietenie.getId() + " adaugata cu succes!");
    }

    private void removeUser(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Numar de parametrii invalid!");
            return;
        }

        Utilizator user_sters = usersService.remove(parts[1]);
        if(user_sters != null)
            System.out.println(user_sters + " si prieteniile acestuia au fost sterse cu succes!");
        else
            System.out.println("Utilizatorul nu a putut fi sters!");
    }

}
