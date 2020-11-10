package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class ObjectCopyTest {

    @Test
    public void main() {
        final ComplicatedTestObject first = new ComplicatedTestObject();
        final ComplicatedTestObject second = new ComplicatedTestObject();

        assert first.number1.equals(second.number1);
        assert first.number2.equals(second.number2);
        assert first != second;
        assert first.object != second.object;

        second.number1--;
        second.number2++;

        Overlord.shallowCopy(first, second);

        assert first.number1 == second.number1;
        assert first.number2 == second.number2;
        assert first != second;
        assert first.object == second.object;
    }

    @Test
    public void complex() {
        final ComplicatedTestObject first = new ComplicatedTestObject();
        final ComplicatedTestObject second = new ComplicatedTestObject();

        assert first.number1.equals(second.number1);
        assert first.number2.equals(second.number2);
        assert first != second;
        assert first.object != second.object;

        second.number1--;
        second.number2++;

        Overlord.deepCopy(first, second);

        assert first.number1 == second.number1;
        assert first.number2 == second.number2;
        assert first != second;
        assert first.object != second.object;
    }

    public static class TestObject {
        long number1 = 100;
        long number2 = 66;
    }

    public static class ComplicatedTestObject {
        Integer number1 = 100;
        Integer number2 = 66;
        String word = "Hello there.";
        TestObject object = new TestObject();
    }

}
