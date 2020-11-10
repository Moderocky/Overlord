package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class CastingTest {

    @Test
    public void main() {
        final TestThing thing = new TestThing();

        final TestObject object = Overlord.transform(thing, TestObject.class);
        Overlord.transform(thing, TestThing.class);

        assert thing.number == 5;
        assert object.getClass() == (Class<?>) TestThing.class;
        assert thing.getClass() == (Class<?>) TestThing.class;
        assert thing == (Object) object;
    }

    @Test
    public void returnTest() {
        final TestThing thing = new TestThing();
        final TestObject object = cast(thing);

        assert thing.number == 5;
        assert object.getClass() == (Class<?>) TestThing.class;
        assert thing.getClass() == (Class<?>) TestThing.class;
        assert thing == (Object) object;
    }

    private TestObject cast(TestThing thing) {
        final TestObject object = Overlord.transform(thing, TestObject.class);
        Overlord.transform(thing, TestThing.class);
        return object;
    }

    private static class TestObject extends TestSuperclass {


    }

    private static class TestThing {

        int number = 5;

    }

    private static class TestSuperclass {

        int number = 10;

    }

}
