package mx.kenzie.overlord;

/**
 * A reference pointer to an object stored in raw memory.
 *
 * @author Moderocky
 * @see Overlord#createPointer(Class)
 * @see Overlord#createPointer(Object)
 */
public class Pointer<T> {

    public final long address;
    public final Class<T> type;

    Pointer(long address, Class<T> type) {
        this.address = address;
        this.type = type;
    }

    public void assign(Object value) {
        Overlord.assign(value, address);
    }

    public T value() {
        return Overlord.retrieve(type, address);
    }

    public void dispose() {
        Overlord.UNSAFE.freeMemory(address);
    }


}
