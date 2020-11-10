package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

import java.util.Objects;

public class ObjectEqualityTest {


    @Test
    public void main() {
        TestObject original = new TestObject();

        long address = Overlord.store(original);

        TestObject thing = Overlord.retrieve(TestObject.class, address);

        Overlord.dispose(address);

        assert thing.equals(original);
        assert thing != original;
        assert thing.field == 15;
        assert thing.number == 66;
    }

    static class TestObject {
        int field = 10;
        long number = 66;

        public TestObject() {
            field = 15;
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, number);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestObject)) return false;
            TestObject thing = (TestObject) o;
            return field == thing.field &&
                    number == thing.number;
        }
    }

}
