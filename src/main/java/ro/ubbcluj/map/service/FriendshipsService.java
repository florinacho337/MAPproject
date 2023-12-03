package ro.ubbcluj.map.service;

import ro.ubbcluj.map.domain.entities.FriendRequest;
import ro.ubbcluj.map.domain.entities.Prietenie;
import ro.ubbcluj.map.domain.entities.Tuple;
import ro.ubbcluj.map.domain.entities.Utilizator;
import ro.ubbcluj.map.repository.dbrepositories.FriendRequestDBRepo;
import ro.ubbcluj.map.repository.dbrepositories.FriendshipDBRepository;
import ro.ubbcluj.map.repository.dbrepositories.UserDBRepository;
import ro.ubbcluj.map.utils.Constants;
import ro.ubbcluj.map.utils.events.ChangeEventType;
import ro.ubbcluj.map.utils.events.UtilizatorChangeEvent;
import ro.ubbcluj.map.utils.exceptions.DuplicateException;
import ro.ubbcluj.map.utils.observer.Observable;
import ro.ubbcluj.map.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class FriendshipsService implements Service<Tuple<Long, Long>, Prietenie>, Observable<UtilizatorChangeEvent> {
//    InMemoryRepository<Tuple<Long, Long>, Prietenie> repoFriendships;
//    InMemoryRepository<Long, Utilizator> repoUsers;
//    InMemoryRepository<Long, FriendRequest> repoFriendRequests;

    FriendshipDBRepository repoFriendships;
    UserDBRepository repoUsers;
    FriendRequestDBRepo repoFriendRequests;
    int startingNode;
    private static long maxID;
    private final List<Observer<UtilizatorChangeEvent>> observers = new ArrayList<>();

    public FriendshipsService(FriendshipDBRepository repoFriendships, UserDBRepository repoUsers, FriendRequestDBRepo repoFriendRequests) {
        this.repoFriendships = repoFriendships;
        this.repoUsers = repoUsers;
        this.repoFriendRequests = repoFriendRequests;
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
    public Prietenie add(Prietenie E) {
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
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
        return null;
    }

    @Override
    public Prietenie remove(Tuple<Long, Long> id) {
        Optional<Prietenie> prietenie;
        if((prietenie = repoFriendships.delete(id)).isPresent()) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.DELETE, null));
            return prietenie.get();
        }
        return null;
    }

    @Override
    public Prietenie find(Tuple<Long, Long> id) {
        Optional<Prietenie> prietenie = repoFriendships.findOne(id);
        return prietenie.orElse(null);
    }

    @Override
    public Prietenie update(Prietenie entity) {
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
        if (Objects.equals(list.get(0), list.get(list.size()-1)) && list.size() > 1)
            list.remove(list.size()-1);
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
        LocalDateTime friendsFrom = prietenie.getDate();
        if(friendsFrom.getMonth().getValue() == luna) {
            if (id_user.equals(u1.getId()))
                return new Tuple<>(u2, friendsFrom.format(Constants.DATE_TIME_FORMATTER));
            else
                return new Tuple<>(u1, friendsFrom.format(Constants.DATE_TIME_FORMATTER));
        }
        return null;
    }

    public FriendRequest sendFriendRequest(Utilizator from, Utilizator to){
        Predicate<FriendRequest> exista = friendRequest -> {
            if(Objects.equals(friendRequest.getFrom().getId(), from.getId()) && Objects.equals(friendRequest.getTo().getId(), to.getId()) && !Objects.equals(friendRequest.getStatus(), "rejected"))
                return true;
            return Objects.equals(friendRequest.getTo().getId(), from.getId()) && Objects.equals(friendRequest.getFrom().getId(), to.getId()) && !Objects.equals(friendRequest.getStatus(), "rejected");
        };
        List <FriendRequest> friendRequests = StreamSupport.stream(getFriendRequests().spliterator(), false)
                .filter(exista).toList();
        if (!friendRequests.isEmpty())
            throw new DuplicateException("Exista o cerere de prietenie acceptata sau in curs de raspuns!");
        FriendRequest friendRequest = new FriendRequest(from, to);
        long id = 0;
        while(repoFriendRequests.findOne(id).isPresent())
            id++;
        friendRequest.setId(id);
        Optional<FriendRequest> friendRequest1 = repoFriendRequests.save(friendRequest);
        if(friendRequest1.isPresent())
            return friendRequest1.get();
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD, null));
        return null;
    }

    public FriendRequest acceptFriendRequest(FriendRequest friendRequest){
        if(friendRequest == null)
            return null;
        Predicate<FriendRequest> exista = getExista(friendRequest);
        List <FriendRequest> friendRequests = StreamSupport.stream(getFriendRequests().spliterator(), false)
                .filter(exista).toList();
        if (!friendRequests.isEmpty())
            throw new DuplicateException("Exista o cerere de prietenie deja acceptata!");
        add(new Prietenie(friendRequest.getFrom(), friendRequest.getTo()));
        friendRequest.setStatus("approved");
        repoFriendRequests.update(friendRequest);
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.UPDATE, null));
        return friendRequest;
    }

    private static Predicate<FriendRequest> getExista(FriendRequest friendRequest) {
        Utilizator from = friendRequest.getFrom();
        Utilizator to = friendRequest.getTo();
        return friendRequest1 -> {
            if(Objects.equals(friendRequest1.getFrom().getId(), from.getId()) && Objects.equals(friendRequest1.getTo().getId(), to.getId()) && Objects.equals(friendRequest1.getStatus(), "approved"))
                return true;
            return Objects.equals(friendRequest1.getTo().getId(), from.getId()) && Objects.equals(friendRequest1.getFrom().getId(), to.getId()) && Objects.equals(friendRequest1.getStatus(), "approved");
        };
    }

    public FriendRequest rejectFriendRequest(FriendRequest friendRequest){
        if(friendRequest == null)
            return null;
        friendRequest.setStatus("rejected");
        repoFriendRequests.update(friendRequest);
        notifyObservers(new UtilizatorChangeEvent(ChangeEventType.UPDATE, null));
        return friendRequest;
    }

    public FriendRequest findFriendRequest(Long id){
        if(repoFriendRequests.findOne(id).isPresent())
            return repoFriendRequests.findOne(id).get();
        return null;
    }

    public Iterable<FriendRequest> getFriendRequests(){return repoFriendRequests.findAll();}

    @Override
    public void addObserver(Observer<UtilizatorChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UtilizatorChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UtilizatorChangeEvent t) {
        observers.forEach(x -> x.update(t));
    }
}
