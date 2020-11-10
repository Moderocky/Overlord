package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class SwapConstructorTest {

    @Test
    public void main() {
        Class1 class1 = new Class1("hoi");

        assert class1.number == 5;
        assert class1.name.equalsIgnoreCase("hoi <- thing");
    }

    @Test
    public void swapped() {
        Class1 class1 = Overlord.createSwapConstructor(Class1.class, Class2.class, "hello");

        assert class1.number == 10;
        assert class1.name.equalsIgnoreCase("hello");
    }

    @Test
    public void another() {
        Class3 class3 = Overlord.createSwapConstructor(Class3.class, Object.class);

        assert class3.number == 0;
        assert class3.name == null;
    }

    static class Class3 {

        public final int number;
        public final String name;

        public Class3(String string) {
            number = 60;
            name = string + " blob";
        }

    }

    static class Class2 {

        public final int number;
        public final String name;

        public Class2(String string) {
            number = 10;
            name = string;
        }

    }

    static class Class1 {

        public final int number;
        public final String name;

        public Class1(String string) {
            number = 5;
            name = string + " <- thing";
        }

    }

}
