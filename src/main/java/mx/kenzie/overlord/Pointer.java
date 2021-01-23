package mx.kenzie.overlord;

/**
 * A reference pointer to an object stored in raw memory.
 *
 * @author Moderocky
 * @see Overlord#createPointer(Class)
 * @see Overlord#createPointer(Object)
 */
record Pointer<T>(long address, Class<T> type) {
    
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
