package unionfindset;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnionFindSetTest<T> {

    @Test
    public void unionFindInteger() {
        UnionFind<Integer> u=new UnionFind<>();
        Integer[] array = {0,1,2,3,4};

        u.makeSet(array);
        u.union(1,2);
        u.union(1,2);
        u.union(3,4);
        u.union(1,0);
        u.union(1,3);
        assertEquals(1, u.findSet(0).intValue());
        assertEquals(1, u.findSet(1).intValue());
        assertEquals(1, u.findSet(2).intValue());
        assertEquals(1, u.findSet(3).intValue());
        assertEquals(1, u.findSet(4).intValue());

        //
        u.makeSet(array);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(1, u.findSet(1).intValue());
        assertEquals(2, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(4, u.findSet(4).intValue());
        u.union(3,2);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(1, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(4, u.findSet(4).intValue());
        u.union(2,1);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(4, u.findSet(4).intValue());
        u.union(1,3);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(4, u.findSet(4).intValue());
        u.union(4,3);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());
        u.union(4,1);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());
        u.union(3,3);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());
        u.union(4,4);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());

        //second tree
        u.union(0,0);
        assertEquals(0, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());

        //union trees
        u.union(0,1);
        assertEquals(3, u.findSet(0).intValue());
        assertEquals(3, u.findSet(1).intValue());
        assertEquals(3, u.findSet(2).intValue());
        assertEquals(3, u.findSet(3).intValue());
        assertEquals(3, u.findSet(4).intValue());
    }

    @Test
    public void unionFindString() {
        UnionFind<String> u=new UnionFind<>();
        String[] array = {"abc","cab","bca","bac","acb"};

        u.makeSet(array);
        u.union("abc","cab");
        u.union("abc","cab");
        u.union("bca","bac");
        u.union("abc","acb");
        u.union("abc","bca");
        assertEquals("abc", u.findSet("acb"));
        assertEquals("abc", u.findSet("abc"));
        assertEquals("abc", u.findSet("cab"));
        assertEquals("abc", u.findSet("bca"));
        assertEquals("abc", u.findSet("bac"));

        //
        u.makeSet(array);
        assertEquals("acb", u.findSet("acb"));
        assertEquals("abc", u.findSet("abc"));
        assertEquals("cab", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bac", u.findSet("bac"));
        u.union("bca","cab");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("abc", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bac", u.findSet("bac"));
        u.union("cab","abc");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bac", u.findSet("bac"));
        u.union("abc","bca");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bac", u.findSet("bac"));
        u.union("bac","bca");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));
        u.union("bac","abc");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));
        u.union("bca","bca");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));
        u.union("bac","bac");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));

        //second tree
        u.union("acb","acb");
        assertEquals("acb", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));

        //union trees
        u.union("acb","abc");
        assertEquals("bca", u.findSet("acb"));
        assertEquals("bca", u.findSet("abc"));
        assertEquals("bca", u.findSet("cab"));
        assertEquals("bca", u.findSet("bca"));
        assertEquals("bca", u.findSet("bac"));
    }

    @Test
    public void unionFindForeignObj() {
        UnionFind<String> u=new UnionFind<>();
        String[] array = {"abc","cab","bca"};

        assertNull(u.findSet("aaa"));
        assertNull(u.findSet("abc"));

        u.makeSet(array);
        assertNotNull(u.findSet("abc"));

        u.makeSet("a");
        assertNotNull(u.findSet("a"));
        assertEquals(4, u.getParents().size());
        assertEquals(4, u.getRanks().size());

        u.union("abc", "cab");
        try {
            u.union("abc", "cab");
        } catch (NullPointerException e){
            //success
        }
        assertEquals(4, u.getParents().size());
        assertEquals(4, u.getRanks().size());
    }

    @Test
    public void makeSetNull(){
        UnionFind<String> u=new UnionFind<>();
        String[] array = {"abc",null,"bca"};

        //findSetTest
        assertNull(u.findSet(null));
        try {
            u.makeSet(array);
        } catch (NullPointerException e){
            //success
        }
        assertNull(u.findSet(null));
    }

    @Test
    public void unionNull(){
        UnionFind<String> u=new UnionFind<>();
        String[] array = {"abc","bca"};

        try {
            u.union(null, null);
        } catch (NullPointerException e){
            //success
        }

        u.makeSet(array);

        try {
            u.union(null, null);
            u.union("bca", null);
            u.union(null, "abc");
        } catch (NullPointerException e){
            //success
        }
    }
}
