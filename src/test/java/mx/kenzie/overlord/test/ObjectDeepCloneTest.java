package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class ObjectDeepCloneTest {

    @Test
    public void main() {
        final TestObject source = new TestObject();
        final TestObject clone = Overlord.deepClone(source);

        assert source.number1 == clone.number1;
        assert source.number2 == clone.number2;
        assert source != clone;
    }

    @Test
    public void complex() {
        final ComplicatedTestObject source = new ComplicatedTestObject();
        final ComplicatedTestObject clone = Overlord.deepClone(source);

        assert source.number1.equals(clone.number1);
        assert source.number2.equals(clone.number2);
        assert source.word.equals(clone.word);
        assert source.object != clone.object;
        assert source.inner != clone.inner;
        assert source.inner.object3 != clone.inner.object3;
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
        InnerTestObject inner = new InnerTestObject();
    }

    public static class InnerTestObject {
        TestObject object1 = new TestObject();
        TestObject object2 = new TestObject();
        TestObject object3 = new TestObject();
    }

}
