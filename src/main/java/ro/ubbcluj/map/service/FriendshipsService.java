package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.Prietenie;
import ro.ubbcluj.map.domain.Tuple;
import ro.ubbcluj.map.domain.Utilizator;
import ro.ubbcluj.map.repository.InMemoryRepository;

import java.util.*;

public class FriendshipsService implements Service<Tuple<Long, Long>, Prietenie> {
    InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships;
    InMemoryRepository<Long, Utilizator> repoUsers;
    int startingNode;
    private static long maxID;

    public FriendshipsService(InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships, InMemoryRepository<Long, Utilizator> repoUsers) {
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
        Utilizator user = null;
        List<Utilizator> friends = new ArrayList<>();
        ArrayList<Tuple<Long, Long>> deSters = new ArrayList<>();
        if(repoUsers.findOne(id_user).isPresent())
            user = repoUsers.findOne(id_user).get();
        if(user != null)
            friends = user.getFriends();
        Utilizator finalUser = user;
        friends.forEach(friend -> {
            long id_friend = friend.getId();
            Tuple<Long, Long> id;
            if(id_user < id_friend)
                id = new Tuple<>(id_user, id_friend);
            else
                id = new Tuple<>(id_friend, id_user);
            deSters.add(id);
            friend.removeFriend(finalUser);
        });
        deSters.forEach(this::remove);
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
}
