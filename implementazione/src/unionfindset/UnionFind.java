package unionfindset;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * UnionFind is used to build a structure merging two trees
 * It manages generic obj and it have't length limits
 */
public class UnionFind<T> {
    private Map<T, T> parents = new HashMap<>();
    private Map<T, Integer> ranks = new HashMap<>();

    /*
     * START getter/setter
     */
    public Map<T, T> getParents() {
        return parents;
    }

    public void setParents(Map<T, T> parents) {
        this.parents = parents;
    }

    public Map<T, Integer> getRanks() {
        return ranks;
    }

    public void setRanks(Map<T, Integer> ranks) {
        this.ranks = ranks;
    }
    /*
     * END getter/setter
     */

    /**
     * @param u1 The first tree
     * @param u2 The second tree
     */
    public void union(@NotNull T u1, @NotNull T u2){

        Link(findSet(u1), findSet(u2));
    }

    /**
     * @param u1 first node
     * @param u2 second node
     */
    private void Link(@NotNull T u1, @NotNull T u2) throws NullPointerException {
        int rank1, rank2;

        if(ranks.get(u1) == null || ranks.get(u2) == null) throw new NullPointerException();

        rank1 = ranks.get(u1);
        rank2 = ranks.get(u2);

        if(rank1 < rank2)
            parents.put(u1, u2);
        else{
            parents.put(u2, u1);
            if(rank1 == rank2)
                ranks.put(u1, ranks.get(u1)+1);
        }
    }

    /**
     * MakeSet builds one or more nodes tree
     * @param element node
     */
    public void makeSet(@NotNull T element) throws NullPointerException {
        if(element == null) throw new NullPointerException();

        parents.put(element, element);
        ranks.put(element, 0);
    }

    /**
     * MakeSet builds one or more nodes tree
     * @param elements nodes
     */
    public void makeSet(@NotNull ArrayList<T> elements) throws NullPointerException {
        for (T element : elements) {
            if(element == null) throw new NullPointerException();

            parents.put(element, element);
            ranks.put(element, 0);
        }
    }

    /**
     * MakeSet builds one or more nodes tree
     * @param elements nodes
     */
    public void makeSet(@NotNull T[] elements) throws NullPointerException {
        for (T element : elements) {
            if(element == null) throw new NullPointerException();

            parents.put(element, element);
            ranks.put(element, 0);
        }
    }

    /**
     * FindSet looks for the root
     * @param u node
     * @return the root
     */
    public T findSet( T u){

        if(parents.get(u) == null)
            return null;

        if(!u.equals(parents.get(u)))
            parents.put(u, findSet(parents.get(u)));
        return parents.get(u);
    }

    @Override
    public String toString(){
        return "<UnionFind\np " + parents.toString() + "\nr " + ranks.toString() + "\n>";
    }
}