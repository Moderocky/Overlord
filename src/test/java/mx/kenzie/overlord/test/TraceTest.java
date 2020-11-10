package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

import java.util.function.Consumer;

public class TraceTest {

    @Test
    public void main() {
        assert Overlord.whereAmI().equals(TraceTest.class);

        TraceReceiver.receiveTraceTest();

        TestSubclass.testSubclassMethodLocator();

        TestSubclass.FUNCTIONAL_INTERFACE_TEST.accept("hello there!");

        TestSubclass2.testFILoc();
    }

    public static class TestSubclass {

        public static final Consumer<String> FUNCTIONAL_INTERFACE_TEST = s -> {
            assert Overlord.whereAmI().equals(TestSubclass.class);
            assert Overlord.whereWasI().equals(TraceTest.class);
        };

        public static void testSubclassMethodLocator() {
            assert Overlord.whereAmI().equals(TestSubclass.class);
        }

    }

    public static class TestSubclass2 {

        public static final Consumer<String> LOCATION_TEST = TestSubclass.FUNCTIONAL_INTERFACE_TEST;

        public static void testFILoc() {
            AssertionError error = null;
            try {
                LOCATION_TEST.accept("blob");
            } catch (AssertionError e) {
                error = e;
            }
            assert error != null;
        }

    }

}
