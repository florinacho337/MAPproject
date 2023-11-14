package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Prietenie;
import ro.ubbcluj.map.domain.Tuple;
import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.repository.FriendshipDBRepository;
import ro.ubbcluj.map.repository.UserDBRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FriendshipsService implements Service<Tuple<Long, Long>, Prietenie> {
//    InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships;
//    InMemoryRepository<Long, Utilizator> repoUsers;

    FriendshipDBRepository repoFriendships;
    UserDBRepository repoUsers;
    int startingNode;
    private static long maxID;

    public FriendshipsService(FriendshipDBRepository repoFriendships, UserDBRepository repoUsers) {
        this.repoFriendships = repoFriendships;
        this.repoUsers = repoUsers;
    }

    private long getMaxID() {
        maxID = 0;
        repoUsers.findAll().forEach(u -> {
            if(maxID < u.getId())
                maxID = u.getId();
        });
        maxID += 1;
        return maxID;
    }

    @Override
    public void add(Prietenie E) {
        Utilizator u1 = E.getU1();
        Utilizator u2 = E.getU2();
        //pentru evitarea cazurilor (u1, u2) si (u2, u1) considerate prietenii diferite
        Tuple<Long, Long> id;
        if (Objects.requireNonNull(u1).getId() < Objects.requireNonNull(u2).getId())
            id = new Tuple<>(u1.getId(), u2.getId());
        else
            id = new Tuple<>(u2.getId(), u1.getId());

        E.setId(id);
        if (repoFriendships.save(E).isPresent())
            throw new DuplicateException("Prietenie deja existenta!");
        u1.addFriend(u2);
        u2.addFriend(u1);
    }

    @Override
    public Prietenie remove(Tuple<Long, Long> id) {
        Utilizator u1 = null, u2 = null;
        if(repoUsers.findOne(id.getLeft()).isPresent())
            u1 = repoUsers.findOne(id.getLeft()).get();
        if(repoUsers.findOne(id.getRight()).isPresent())
            u2 = repoUsers.findOne(id.getRight()).get();
        if(u1 != null && u2 != null) {
            u1.removeFriend(u2);
            u2.removeFriend(u1);
        }
        return repoFriendships.delete(id).get();
    }

    public void removePrietenii(Long id_user) {
        Utilizator user;
        List<Utilizator> friends;
        if(repoUsers.findOne(id_user).isPresent())
            user = repoUsers.findOne(id_user).get();
        else {
            user = null;
        }
        friends = Objects.requireNonNull(user).getFriends();

        List<Tuple<Long, Long>> deSters = friends.stream()
                .map(friend -> collectFriendshipsToRemove(id_user, friend))
        .toList();
        deSters.forEach(this::remove);
        deSters.forEach(id_friend -> {
            if(Objects.equals(id_friend.getLeft(), id_user))
                Objects.requireNonNull(user).removeFriend(repoUsers.findOne(id_friend.getRight()).get());
            else
                Objects.requireNonNull(user).removeFriend(repoUsers.findOne(id_friend.getLeft()).get());
        });


    }

    private static Tuple<Long, Long> collectFriendshipsToRemove(Long id_user, Utilizator friend) {
        long id_friend = friend.getId();
        if(id_user < id_friend)
            return new Tuple<>(id_user, id_friend);
        else
            return new Tuple<>(id_friend, id_user);
//        deSters.add(id);
//        friend.removeFriend(finalUser);
    }

    @Override
    public Prietenie find(Tuple<Long, Long> id) {
        if(repoFriendships.findOne(id).isPresent())
            return repoFriendships.findOne(id).get();
        return null;
    }

    @Override
    public Iterable<Prietenie> getAll() {
        return repoFriendships.findAll();
    }

    private int DFSUtil(int id, boolean[] visited, boolean biggestconn, ArrayList<Integer> lista) {
        visited[id] = true;
        final int[] componentSize = {1};
        List<Utilizator> friends = null;

        if (biggestconn)
            lista.add(id);

        if(repoUsers.findOne((long) id).isPresent())
            friends = repoUsers.findOne((long) id).get().getFriends();
        if(friends != null) {
            friends.forEach(friend -> {
                int n = Math.toIntExact(friend.getId());
                if (!visited[n])
                    componentSize[0] += DFSUtil(n, visited, biggestconn, lista);
            });
        }
        return componentSize[0];
    }

    private int DFSUtil2(int id, boolean[] visited, boolean biggestconn, ArrayList<Integer> lista) {
        visited[id] = true;
        int componentSize = 1;

        if(biggestconn)
            lista.add(id);

        List<Utilizator> friends = null;
        ListIterator<Utilizator> i = null;
        if(repoUsers.findOne((long) id).isPresent())
            friends = repoUsers.findOne((long) id).get().getFriends();
        if(friends != null)
            i = friends.listIterator();
        if (i != null && i.hasNext()) {
            int n = Math.toIntExact(i.next().getId());
            if (!visited[n])
                componentSize += DFSUtil2(n, visited, biggestconn, lista);
        }
        return componentSize;
    }

    public int connectedComponents() {
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        int connected = 0;
        startingNode = 0;
        for (int v = 0; v < getMaxID(); v++) {
            if (repoUsers.findOne((long) v).isEmpty())
                continue;
            if (!visited[v]) {
                DFSUtil(v, visited, false, null);
                connected++;
            }
        }
        return connected;
    }

    private void setStartingNode() {
        startingNode = 0;
        int maxComponentSize = 0;
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        for (int v = 0; v < getMaxID(); ++v) {
            if (repoUsers.findOne((long) v).isEmpty())
                continue;
            if (!visited[v]) {
                int componentSize = DFSUtil2(v, visited, false, null);
                if (componentSize > maxComponentSize) {
                    maxComponentSize = componentSize;
                    startingNode = v;
                }
            }
            Arrays.fill(visited, false);
        }
    }

    public ArrayList<Integer> biggestConnectedComponent() {
        ArrayList<Integer> list = new ArrayList<>();
        setStartingNode();
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        DFSUtil(startingNode, visited, true, list);
        if (Objects.equals(list.getFirst(), list.getLast()) && list.size() > 1)
            list.removeLast();
        return list;
    }

    public List<Tuple<Utilizator, String>> prieteniiDinLuna(Long id_user, int luna){
        Utilizator utilizator = null;
        if(repoUsers.findOne(id_user).isPresent())
            utilizator = repoUsers.findOne(id_user).get();
        List<Utilizator> prieteniUser = Objects.requireNonNull(utilizator).getFriends();
        return prieteniUser.stream()
                .map(prieten -> collectFriendships(id_user, prieten, luna))
                .filter(Objects::nonNull)
                .toList();
    }

    private Tuple<Utilizator, String> collectFriendships(Long id_user, Utilizator prieten, int luna) {
        Long id_prieten = prieten.getId();
        Tuple<Long, Long> id_prietenie;
        if(id_user < id_prieten)
            id_prietenie = new Tuple<>(id_user, id_prieten);
        else
            id_prietenie = new Tuple<>(id_prieten, id_user);
        Prietenie prietenie = null;
        if(repoFriendships.findOne(id_prietenie).isPresent())
            prietenie = repoFriendships.findOne(id_prietenie).get();
        Utilizator u1 = Objects.requireNonNull(prietenie).getU1();
        Utilizator u2 = prietenie.getU2();
        LocalDate friendsFrom = prietenie.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if(friendsFrom.getMonth().getValue() == luna) {
            if (id_user.equals(u1.getId()))
                return new Tuple<>(u2, friendsFrom.format(formatter));
            else
                return new Tuple<>(u1, friendsFrom.format(formatter));
        }
        return null;
    }
}
