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

    public FriendshipsService(InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships, InMemoryRepository<Long, Utilizator> repoUsers) {
        this.repoFriendships = repoFriendships;
        this.repoUsers = repoUsers;
    }

    private long getMaxID(){
        long maxID = 0;
        for(Utilizator u: repoUsers.findAll())
            if(maxID < u.getId())
                maxID = u.getId();
        maxID += 1;
        return maxID;
    }

    @Override
    public void add(Prietenie E) {
        Utilizator u1 = E.getU1();
        Utilizator u2 = E.getU2();
        if(u1 ==null || u2 == null)
            repoFriendships.save(null);
        //pentru evitarea cazurilor (u1, u2) si (u2, u1) considerate prietenii diferite
        Tuple<Long, Long> id;
        if (u1.getId() < u2.getId())
            id = new Tuple<>(u1.getId(), u2.getId());
        else
            id = new Tuple<>(u2.getId(), u1.getId());

        E.setId(id);
        if (repoFriendships.save(E) != null)
            throw new DuplicateException("Prietenie deja existenta!");
        u1.addFriend(u2);
        u2.addFriend(u1);
    }

    @Override
    public Prietenie remove(Tuple<Long, Long> id) {
        return repoFriendships.delete(id);
    }

    public void removePrietenii(Utilizator user){
        List<Utilizator> friends = user.getFriends();
        Long id_user = user.getId();
        for (Utilizator friend: friends) {
            Long id_friend = friend.getId();
            Tuple<Long, Long> id;
            if (id_user < id_friend)
                id = new Tuple<>(id_user, id_friend);
            else
                id = new Tuple<>(id_friend, id_user);
            remove(id);
            friend.removeFriend(user);
        }
    }

    @Override
    public Prietenie find(Tuple<Long, Long> id) {
        return repoFriendships.findOne(id);
    }

    private int DFSUtil(int id, boolean[] visited, boolean biggestconn, ArrayList<Integer> lista){
        visited[id] = true;
        int componentSize = 1;

        if(biggestconn)
            lista.add(id);

        List<Utilizator> friends = repoUsers.findOne((long) id).getFriends();
        ListIterator<Utilizator> i = friends.listIterator();
        while (i.hasNext()) {
            int n = Math.toIntExact(i.next().getId());
            if (!visited[n])
                componentSize += DFSUtil(n, visited, biggestconn, lista);
        }
        return componentSize;
    }

    private int DFSUtil2(int id, boolean[] visited, boolean biggestconn, ArrayList<Integer> lista){
        visited[id] = true;
        int componentSize = 1;

        if(biggestconn)
            lista.add(id);

        List<Utilizator> friends = repoUsers.findOne((long) id).getFriends();
        ListIterator<Utilizator> i = friends.listIterator();
        while (i.hasNext()) {
            int n = Math.toIntExact(i.next().getId());
            if (!visited[n])
                componentSize += DFSUtil2(n, visited, biggestconn, lista);
            break;
        }
        return componentSize;
    }

    public int connectedComponents(){
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        Integer connected = 0;
        startingNode = 0;
        for(int v = 0; v < getMaxID(); v++){
            if(repoUsers.findOne((long) v) == null)
                continue;
            if(!visited[v]){
                DFSUtil(v, visited, false, null);
                connected++;
            }
        }
        return connected;
    }

    private void setStartingNode(){
        startingNode = 0;
        int maxComponentSize = 0;
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        for(int v = 0; v < getMaxID(); ++v){
            if(repoUsers.findOne((long) v) == null)
                continue;
            if(!visited[v]){
                int componentSize = DFSUtil2(v, visited, false, null);
                if(componentSize > maxComponentSize){
                    maxComponentSize = componentSize;
                    startingNode = v;
                }
            }
        }
    }

    public ArrayList<Integer> biggestConnectedComponent(){
        ArrayList<Integer> list = new ArrayList<>();
        setStartingNode();
        boolean[] visited = new boolean[Math.toIntExact(getMaxID())];
        DFSUtil(startingNode, visited, true, list);
        if(Objects.equals(list.getFirst(), list.getLast()))
            list.removeLast();
        return list;
    }
}
