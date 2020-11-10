package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

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
    }

    @Test
    public void illegal() {
        String string = "Hello there!";
        Integer integer = Overlord.transform(string, Integer.class);

        System.out.println(string.getClass());

        assert (string + "").equals("0");
        assert integer == 0;
        assert string == (Object) integer;
        assert string.getClass() == (Class<?>) Integer.class;
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

