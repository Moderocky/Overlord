package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;
import sun.misc.Unsafe;

public class MemoryTest {

    static Unsafe unsafe = Overlord.UNSAFE;

    @Test
    public void main() {
        final FirstClass stuff = new FirstClass();
        final long[] addresses = getAddresses(stuff);
        int i = unsafe.getInt(addresses[0] + 12);
        long l = unsafe.getLong(addresses[0] + 4 + 12);
        assert i == 10;
        assert l == 20;
        long address = Overlord.getAddress(stuff);
        {
            final FirstClass obj = new FirstClass();
            assert obj.first == 10;
            swapInt(address + 12, 66);
            assert stuff.first == 66;
        }
        attemptTransformation();
    }

    public static long[] getAddresses(Object... objects) {
        long[] longs = new long[objects.length];
        long last;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4 -> {
                long factor = 8;
                longs[0] = last = (unsafe.getInt(objects, offset) & 0xFFFFFFFFL) * factor;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (unsafe.getInt(objects, offset + i * 4) & 0xFFFFFFFFL) * factor;
                    if (i2 > last)
                        longs[i] = i2 - last;
                    else
                        longs[i] = last - i2;
                    last = i2;
                }
            }
            case 8 -> throw new AssertionError("Not supported");
        }
        return longs;
    }

    private static void swapInt(long address, int value) {
        unsafe.putInt(address, value);
    }

    public static void attemptTransformation() {
        final long offset = 8;
        FirstClass first = new FirstClass();
        SecondClass second = new SecondClass();
        long[] addresses = new long[]{Overlord.getAddress(first), Overlord.getAddress(second)};
        assert addresses[0] != addresses[1];
        int[] klass = new int[]{unsafe.getInt(addresses[0] + offset), unsafe.getInt(addresses[1] + offset)};
        assert klass[0] != klass[1];
        unsafe.putInt(addresses[1] + 8L, klass[0]);

        FirstClass cast = (FirstClass) (Object) second;
        assert first.getClass().equals(second.getClass());
        assert cast.getClass().equals(second.getClass());
    }

    public static class FirstClass {
        int first = 10;
        long second = 20;
    }

    public static class SecondClass {
        int first = 5;
        long second = 8;
    }

}
