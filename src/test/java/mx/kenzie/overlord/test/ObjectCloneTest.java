package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class ObjectCloneTest {

    @Test
    public void main() {
        final TestObject source = new TestObject();
        final TestObject clone = Overlord.shallowClone(source);

        assert source.number1 == clone.number1;
        assert source.number2 == clone.number2;
        assert source != clone;
    }

    @Test
    public void complex() {
        final ComplicatedTestObject source = new ComplicatedTestObject();
        final ComplicatedTestObject clone = Overlord.shallowClone(source);

        assert source.number1.equals(clone.number1);
        assert source.number2.equals(clone.number2);
        assert source.word.equals(clone.word);
        assert source.object == clone.object;
        assert source != clone;
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
