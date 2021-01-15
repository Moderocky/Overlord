package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("all")
public class TransformationTest {

    @Test
    public void main() {
        ClassA a = new ClassA();
        assert a.field == 100;
        ClassB b = Overlord.transform(a, ClassB.class);
        assert b.field == 100;
        b.field = 99;
        assert a.field == 99;
        assert a == (Object) b;

        assert a.print().equals("B!");
        assert b.print().equals("B!");

        Throwable ex = null;
        try {
            a.box();
        } catch (Throwable e) {
            ex = e;
        }
        assert ex != null;
        
        var blob = true;
    }

    @Test
    public void illegal() {
        String string = "Hello there!";
        Integer integer = Overlord.transform(string, Integer.class);

        assert ((Object) string instanceof Integer);
        assert (string + "").equals("0");
        assert integer == 0;
        assert string == (Object) integer;
        assert string.getClass() == (Class<?>) Integer.class;
    }

    @Test
    public void complex() {
        HashMap<String, String> map = new HashMap<>();
        map.put("hello", "there");

        TreeMap<?, ?> newMap = Overlord.transform(map, TreeMap.class);
        assert newMap.size() == 1;
        Overlord.transform(map, HashMap.class);
        assert map.get("hello").equals("there");
    }

    @Test
    public void arrays() {
        final String[] strings = new String[10];
        strings[0] = "hello";
        strings[1] = "there";

        final Integer[] integers = Overlord.transform(strings, Integer[].class);
        assert integers.length == 10;

        assert (integers[0] + "").equals("hello");
        assert integers[0].getClass().equals(String.class);

        final Graphics2D[] graphics = Overlord.transform(strings, Graphics2D[].class);
        assert graphics.length == 10;

        assert (graphics[0] + "").equals("hello");
        assert graphics[0].getClass().equals(String.class);

        final long[] longs = Overlord.transform(strings, long[].class);
        assert longs.length == 10;

        Overlord.transform(strings, String[].class);
        assert strings[0].equals("hello");
        assert strings[1].equals("there");
    }

    @Test
    public void generics() {
        final List<String> first = new ArrayList<>();
        first.add("hello");
        first.add("there");

        final List<Object> second = Overlord.transformTypecast(first, ArrayList.class);
        assert second.get(0).equals("hello");
        second.add("general");
        assert first.get(2).equals("general");
        second.add(new Object());
        assert first.size() == 4;
        assert first == (Object) second;
    }

    public static class ClassA {
        public int field = 100;

        public String print() {
            return ("A!");
        }

        public void box() {
            System.out.println("Success!");
        }
    }

    public static class ClassB {
        public int field = 66;

        public String print() {
            return ("B!");
        }
    }

}

