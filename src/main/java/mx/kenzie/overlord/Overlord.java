package mx.kenzie.overlord;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.constant.Constable;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * A library for manipulating raw native memory, objects
 * and various other unsafe things.
 * <p>
 * Control individual aspects of the JVM that would be
 * entirely inaccessible to normal Java programs.
 *
 * @author Moderocky
 */
public final class Overlord {

    /**
     * Sun's Unsafe.
     * Use at own risk! :)
     */
    public static final Unsafe UNSAFE;

    /**
     * The internal reflection factory.
     * Use at own risk! :)
     */
    public static final ReflectionFactory FACTORY;
    public static final boolean IS_COMPRESSED_OOP;
    public static final boolean IS_COMPRESSED_KLASS;
    /**
     * A place to store blind methods used repeatedly.
     */
    static final Method[] METHODS = new Method[16];
    /**
     * Used for contacting other Overlord implementations.
     * Currently not used.
     */
    static final long MALLOC_ADDRESS;
    static final Object VIRTUAL_MACHINE;
    private static final Object JDK_UNSAFE;
    private static Class<?> reflectAccessClass;
    private static Class<?> methodAccessorClass;
    private static Object reflectAccess;
    
    static {
        boolean IS_COMPRESSED_KLASS1;
        boolean IS_COMPRESSED_OOP1;
        Unsafe unsafe;
        try {
            unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                final Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return (Unsafe) f.get(null);
            });
        } catch (PrivilegedActionException e) {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                unsafe = (Unsafe) field.get(null);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                e.addSuppressed(ex);
                throw new RuntimeException(e);
            }
        }
        UNSAFE = unsafe;
        FACTORY = ReflectionFactory.getReflectionFactory();

        Object jdkUnsafe;
        try {
            jdkUnsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                final Field field = Unsafe.class.getDeclaredField("theInternalUnsafe");
                field.setAccessible(true);
                return field.get(null);
            });
        } catch (PrivilegedActionException e) {
            try {
                Field field = Unsafe.class.getDeclaredField("theInternalUnsafe");
                field.setAccessible(true);
                jdkUnsafe = field.get(null);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                e.addSuppressed(ex);
                throw new RuntimeException(e);
            }
        }
        JDK_UNSAFE = jdkUnsafe;

        try {
            HotSpotDiagnosticMXBean bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            IS_COMPRESSED_OOP1 = Boolean.parseBoolean(bean.getVMOption("UseCompressedOops").getValue());
            IS_COMPRESSED_KLASS1 = false;
        } catch (Throwable ex) {
            System.out.println("Could not expose the Virtual Machine options.");
            IS_COMPRESSED_OOP1 = true;
            IS_COMPRESSED_KLASS1 = true;
        }
        
        IS_COMPRESSED_KLASS = IS_COMPRESSED_KLASS1;
        IS_COMPRESSED_OOP = IS_COMPRESSED_OOP1;
        VIRTUAL_MACHINE = null;
        try {
            allowAccess(Class.class);
            METHODS[0] = Class.class.getDeclaredMethod("getDeclaredConstructors0", boolean.class);
            METHODS[0].setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not expose getDeclaredConstructors0.");
        }
        try {
            allowAccess(Module.class);
            METHODS[5] = Module.class
                .getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
            METHODS[5].setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not expose implAddExportsOrOpens.");
        }
        try {
            allowAccess(Unsafe.class);
            breakEncapsulation(JDK_UNSAFE.getClass(), true);
            allowAccess(JDK_UNSAFE.getClass(), true);
            MethodHandles.class.getModule().addOpens(MethodHandles.class.getPackageName(), Overlord.class.getModule());
            METHODS[1] = JDK_UNSAFE.getClass()
                .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
            METHODS[1].setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not expose defineClass.");
        }
        try {
            METHODS[2] = Class.class.getDeclaredMethod("protectionDomain");
            METHODS[2].setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not expose protectionDomain.");
        }
        try {
            allowAccess(MethodHandles.class);
            allowAccess(MethodHandles.Lookup.class);
            METHODS[3] = MethodHandles.Lookup.class.getDeclaredMethod("makeClassDefiner", byte[].class);
            METHODS[3].setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not expose makeClassDefiner.");
        }
        try {
            Class<?> cls = Class.forName("java.lang.reflect.ReflectAccess");
            allowAccess(cls);
            reflectAccessClass = cls;
        } catch (ClassNotFoundException e) {
            reflectAccessClass = null;
            System.out.println("Could not expose ReflectAccess class.");
        }
        try {
            Class<?> cls = Class.forName("jdk.internal.reflect.MethodAccessor");
            breakEncapsulation(cls, true);
            allowAccess(cls, true);
            methodAccessorClass = cls;
        } catch (ClassNotFoundException e) {
            methodAccessorClass = null;
            System.out.println("Could not expose MethodAccessor class.");
        }
        try {
            Field delegate = ReflectionFactory.class.getDeclaredField("delegate");
            delegate.setAccessible(true);
            Object internal = delegate.get(null);
            breakEncapsulation(internal.getClass(), true);
            allowAccess(internal.getClass(), true);
            Field langReflectAccess = internal.getClass().getDeclaredField("langReflectAccess");
            langReflectAccess.setAccessible(true);
            reflectAccess = langReflectAccess.get(internal);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            reflectAccess = null;
            System.out.println("Could not expose JavaLangReflectAccess instance.");
        }
        try {
            METHODS[4] = reflectAccessClass.getDeclaredMethod("newConstructor",
                Class.class,
                Class[].class,
                Class[].class,
                int.class,
                int.class,
                String.class,
                byte[].class,
                byte[].class);
            METHODS[4].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose newConstructor.");
        }
        try {
            METHODS[6] = reflectAccessClass.getDeclaredMethod("getMethodAccessor", Method.class);
            METHODS[6].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose getMethodAccessor.");
        }
        try {
            METHODS[7] = reflectAccessClass.getDeclaredMethod("setMethodAccessor", Method.class, methodAccessorClass);
            METHODS[7].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose setMethodAccessor.");
        }
        try {
            breakEncapsulation(methodAccessorClass, true);
            allowAccess(methodAccessorClass, true);
            METHODS[8] = methodAccessorClass.getDeclaredMethod("invoke", Object.class, Object[].class);
            METHODS[8].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose invoke (MethodAccessor).");
        }
        try {
            allowAccess(Method.class);
            METHODS[9] = Method.class.getDeclaredMethod("getRoot");
            METHODS[9].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose getRoot.");
        }
        try {
            allowAccess(Class.class);
            METHODS[10] = Class.class.getDeclaredMethod("getMethod0", String.class, Class[].class);
            METHODS[10].setAccessible(true);
        } catch (NoSuchMethodException | IllegalStateException e) {
            System.out.println("Could not expose getMethod0.");
        }
    }

    static {
        final String address = System.getProperty("overlord.malloc_address");
        if (address == null) {
            MALLOC_ADDRESS = UNSAFE.allocateMemory(32);
            System.setProperty("overlord.malloc_address", MALLOC_ADDRESS + "");
        } else {
            MALLOC_ADDRESS = Long.parseLong(address);
        }
    }
    
    /**
     * This is an entirely static class as everything it
     * uses is in the global state.
     *
     * @deprecated nothing instance-required
     */
    @Deprecated
    private Overlord() {
        throw new RuntimeException("Should not be used!");
    }

    public static void assign(byte value, long address) {
        UNSAFE.putByte(address, value);
    }

    public static void assign(short value, long address) {
        UNSAFE.putShort(address, value);
    }

    public static void assign(int value, long address) {
        UNSAFE.putInt(address, value);
    }

    public static void assign(long value, long address) {
        UNSAFE.putLong(address, value);
    }

    public static void assign(boolean value, long address) {
        UNSAFE.putByte(address, (byte) (value ? 1 : 0));
    }

    public static void assign(char value, long address) {
        UNSAFE.putChar(address, value);
    }

    public static void assign(float value, long address) {
        UNSAFE.putFloat(address, value);
    }

    public static void assign(double value, long address) {
        UNSAFE.putDouble(address, value);
    }

    /**
     * Assigns an object to the given address.
     * This breaks it up - any non-primitive fields will be stored as memory
     * references and recursively assigned.
     *
     * @param object  the object to assign
     * @param address the address
     */
    @Contract(pure = true, value = "null, _ -> fail")
    public static void assign(Object object, long address) {
        Class<?> cls = object.getClass();
        do {
            for (Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                long offset = UNSAFE.objectFieldOffset(field);
                if (field.getType() == long.class) {
                    assign(UNSAFE.getLong(object, offset), address + offset);
                } else if (field.getType() == int.class) {
                    assign(UNSAFE.getInt(object, offset), address + offset);
                } else if (field.getType() == short.class) {
                    assign(UNSAFE.getShort(object, offset), address + offset);
                } else if (field.getType() == byte.class) {
                    assign(UNSAFE.getByte(object, offset), address + offset);
                } else if (field.getType() == boolean.class) {
                    assign(UNSAFE.getBoolean(object, offset), address + offset);
                } else if (field.getType() == char.class) {
                    assign(UNSAFE.getChar(object, offset), address + offset);
                } else if (field.getType() == float.class) {
                    assign(UNSAFE.getFloat(object, offset), address + offset);
                } else if (field.getType() == double.class) {
                    assign(UNSAFE.getDouble(object, offset), address + offset);
                } else {
                    Object nextObject = UNSAFE.getObject(object, offset);
                    long nextAddress = prepareMemory(nextObject.getClass());
                    assign(nextObject, nextAddress);
                    assign(nextAddress, address + offset);
                }
            }
        } while ((cls = cls.getSuperclass()) != null);
    }

    /**
     * Retrieves (deserializes) an object from memory.
     *
     * @param cls     the class of the object
     * @param address the memory address
     * @param <T>     the object type
     * @return the (new) object
     */
    @SuppressWarnings("unchecked")
    @Contract(pure = true, value = "null, _ -> fail")
    public static <T> T retrieve(Class<T> cls, long address) {
        return (T) retrieve0(cls, address);
    }

    /**
     * Frees up memory at an address.
     * WARNING!!! Doing this to an address inside the heap will
     * murder the JVM.
     *
     * @param address the address to free
     */
    public static void dispose(long address) {
        UNSAFE.freeMemory(address);
    }

    public static void erase(long address, long length) {
        for (int i = 0; i < length; i++) {
            UNSAFE.putByte(address + i, (byte) 0);
        }
    }

    @Contract(pure = true, value = "null, _ -> fail")
    static Object retrieve0(Class<?> cls, long address) {
        Object object;
        try {
            object = UNSAFE.allocateInstance(cls);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        }
        do {
            for (Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                long offset = UNSAFE.objectFieldOffset(field);
                if (field.getType() == long.class) {
                    UNSAFE.putLong(object, offset, UNSAFE.getLong(address + offset));
                } else if (field.getType() == int.class) {
                    UNSAFE.putInt(object, offset, UNSAFE.getInt(address + offset));
                } else if (field.getType() == short.class) {
                    UNSAFE.putShort(object, offset, UNSAFE.getShort(address + offset));
                } else if (field.getType() == byte.class) {
                    UNSAFE.putByte(object, offset, UNSAFE.getByte(address + offset));
                } else if (field.getType() == boolean.class) {
                    UNSAFE.putBoolean(object, offset, UNSAFE.getByte(address + offset) == 1);
                } else if (field.getType() == char.class) {
                    UNSAFE.putChar(object, offset, UNSAFE.getChar(address + offset));
                } else if (field.getType() == float.class) {
                    UNSAFE.putFloat(object, offset, UNSAFE.getFloat(address + offset));
                } else if (field.getType() == double.class) {
                    UNSAFE.putDouble(object, offset, UNSAFE.getDouble(address + offset));
                } else {
                    long nextAddress = UNSAFE.getLong(address + offset);
                    UNSAFE.putObject(object, offset, retrieve(field.getType(), nextAddress));
                }
            }
        } while ((cls = cls.getSuperclass()) != null);
        return object;
    }

    /**
     * Stores an object in memory outside the heap.
     *
     * @param object the object to store
     * @return the memory address
     */
    @Contract(pure = true, value = "null -> fail")
    public static long store(Object object) {
        final long address = prepareMemory(object.getClass());
        assign(object, address);
        return address;
    }

    /**
     * Create a pointer for the given object.
     * Stores the object at the pointer's location.
     * <p>
     * This points to memory OUTSIDE the heap - it will never be garbage
     * collected.
     *
     * @param object the type to store
     * @return the pointer
     * @see Pointer#dispose() for dealing with this.
     */
    @Contract(pure = true, value = "null -> fail")
    public static <T> Pointer<T> createPointer(T object) {
        final Pointer<T> pointer = createPointer((Class<T>) object.getClass());
        pointer.assign(object);
        return pointer;
    }

    /**
     * Create a pointer for the given type.
     * Stores an object of that type.
     * <p>
     * This points to memory OUTSIDE the heap - it will never be garbage
     * collected.
     *
     * @param type the type to store
     * @return the pointer
     * @see Pointer#dispose() for dealing with this.
     */
    @Contract(pure = true, value = "null -> fail")
    public static <T> Pointer<T> createPointer(Class<T> type) {
        final long address = prepareMemory(type);
        return new Pointer<>(address, type);
    }

    /**
     * Allocates memory for this object.
     *
     * @param cls the class
     * @return the memory address
     */
    @Contract(pure = true, value = "null -> fail")
    static long prepareMemory(Class<?> cls) {
        return UNSAFE.allocateMemory(getMemorySize(cls));
    }

    /**
     * Returns the size in memory required to store the given class.
     * This may not be entirely accurate.
     *
     * @param cls the class
     * @return the size in memory required to store the given class
     */
    @Contract(pure = true, value = "null -> fail")
    public static long getMemorySize(Class<?> cls) {
        if (cls == byte.class) return (1);
        if (cls == short.class) return (2);
        if (cls == int.class) return (4);
        if (cls == long.class) return (8);
        if (cls == boolean.class) return (1);
        if (cls == char.class) return (2);
        if (cls == float.class) return (4);
        if (cls == double.class) return (8);
        if (cls == void.class) return (1);
        return getMemorySize0(cls);
    }

    static long getMemorySize0(Object object) {
        Set<Field> fields = new HashSet<>();
        Class<?> cls = object.getClass();
        while (cls != Object.class) {
            for (final Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                fields.add(field);
            }
            cls = cls.getSuperclass();
        }
        long maxSize = 0;
        for (final Field field : fields) {
            final long offset = UNSAFE.objectFieldOffset(field);
            if (offset > maxSize) maxSize = offset;
        }
        return ((maxSize / 8) + 1) * 8;
    }

    /**
     * Creates an instance of the given class.
     * This will try and find a constructor in the copy class that matches
     * the provided parameters.
     * <p>
     * The copy class does NOT have to be related to the given class.
     * Dynamic fields will be set by name/type.
     * <p>
     * Be careful with final fields - they may be held in swap memory.
     *
     * @param cls        the given class
     * @param copyClass  the copy class to search for a constructor
     * @param parameters the parameters to use
     * @param <T>        the type of the object
     * @return the new instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createSwapConstructor(Class<T> cls, Class<?> copyClass, Object... parameters) {
        try {
            Constructor<?>[] constructors = (Constructor<?>[]) METHODS[0].invoke(copyClass, false);
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() != parameters.length) continue;
                Class<?>[] types = constructor.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].isAssignableFrom(parameters[i].getClass())) continue;
                    Constructor<T> copyConstructor = (Constructor<T>) FACTORY
                            .newConstructorForSerialization(cls, constructor);
                    copyConstructor.setAccessible(true);
                    return copyConstructor.newInstance(parameters);
                }
            }
            return createSwapConstructor(cls, copyClass.getConstructor());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates an instance of the given class using the provided constructor
     * and parameters.
     * The given constructor does NOT have to be from the given class.
     * Dynamic fields will be set by name/type.
     * <p>
     * Be careful with final fields - they may be held in swap memory.
     *
     * @param cls         the given class
     * @param constructor the constructor to use
     * @param parameters  the parameters to use
     * @param <T>         the type of the object
     * @return the new instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createSwapConstructor(Class<T> cls, Constructor<?> constructor, Object... parameters) {
        try {
            Constructor<T> copyConstructor = (Constructor<T>) FACTORY
                    .newConstructorForSerialization(cls, constructor);
            copyConstructor.setAccessible(true);
            return copyConstructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * The same as above, but will fail if the fields are not matching.
     * Having matching fields is important, as it MAY change the order
     * of the fields in memory and so the new schema will retrieve from
     * the wrong offsets.
     *
     * @param object the object to transform
     * @param cls    the target class
     * @param <T>    the target type
     * @return the object, cast to the target type
     */
    @Contract(value = "null, _ -> null")
    public static <T> T transformSafe(Object object, Class<T> cls) {
        if (object == null) return null;
        if (!hasMatchingFieldErasure(object.getClass(), cls)) return null;
        return transform(object, cls);
    }

    /**
     * Determines whether the two given classes have matching fields.
     * This includes order, type, etc.
     *
     * @param a first class to compare
     * @param b second class to compare
     * @return true if they do
     */
    public static boolean hasMatchingFieldErasure(Class<?> a, Class<?> b) {
        final Field[] first = a.getDeclaredFields();
        final Field[] second = b.getDeclaredFields();
        if (first.length != second.length) return false;
        for (int i = 0; i < first.length; i++) {
            if (!first[i].getClass().equals(second[i].getClass())) return false;
        }
        return true;
    }

    /**
     * Transforms an object to another class.
     * The original object WILL be transformed.
     * <p>
     * The return value is the SAME object, but
     * cast to its new class.
     * <p>
     * Treating the object as the original is fine pre-compilation.
     * Trying to use a method not present on the new type will throw an
     * exception.
     *
     * @param object the object to transform
     * @param cls    the target class
     * @param <T>    the target type
     * @return the object, cast to the target type
     */
    @Contract(value = "null, _ -> null")
    @SuppressWarnings("unchecked")
    public static <T> T transform(Object object, Class<? super T> cls) {
        if (object == null) return null;
        final T template = (T) createEmpty(cls);
        final long offset = 8;
        final long[] addresses = new long[]{
            getAddress(template),
            getAddress(object)
        };
        final int[] klass = new int[]{
            UNSAFE.getInt(addresses[0] + offset),
            UNSAFE.getInt(addresses[1] + offset)
        };
        UNSAFE.putInt(addresses[1] + offset, klass[0]);
        return (T) object;
    }

    /**
     * Creates an empty instance of this class.
     * Skips any constructors.
     *
     * @param cls the target class
     * @param <T> the target type
     * @return the new instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createEmpty(Class<T> cls) {
        try {
            return (T) UNSAFE.allocateInstance(cls);
        } catch (InstantiationException ex) {
            if (cls.isArray()) {
                return (T) Array.newInstance(cls.getComponentType(), 0);
            } else if (cls.isEnum()) {
                return cls.getEnumConstants()[0];
            } else if (cls.isPrimitive()) {
                return null;
            }
            throw new RuntimeException(ex);
        }
    }
    
    @Contract(value = "null, _ -> null")
    @SuppressWarnings("unchecked")
    public static <T> T transformTypecast(Object object, Class<?> cls) {
        if (object == null) return null;
        final T template = (T) createEmpty(cls);
        final long offset = 8;
        final long[] addresses = new long[]{
            getAddress(template),
            getAddress(object)
        };
        final int[] klass = new int[]{
            UNSAFE.getInt(addresses[0] + offset),
            UNSAFE.getInt(addresses[1] + offset)
        };
        UNSAFE.putInt(addresses[1] + offset, klass[0]);
        return (T) object;
    }
    
    /**
     * Finds the memory address of the given object.
     * This is done by creating an array and retrieving the
     * reference from the array.
     * <p>
     * NOTE: memory addresses may be reassigned without warning!
     *
     * @param object the object.
     * @return the address of the object in the heap
     */
    public static long getAddress(Object object) {
        final Object[] objects = new Object[]{object};
        final int offset = UNSAFE.arrayBaseOffset(objects.getClass());
        final int scale = UNSAFE.arrayIndexScale(objects.getClass());
        return switch (scale) {
            case 4 -> (UNSAFE.getInt(objects, offset) & 0xFFFFFFFFL) * 8;
//            case 8 -> // TODO: 09/11/2020 Add impl for 8-scaled arrays?
            default -> throw new IllegalStateException("Unexpected value: " + scale);
        };
    }

    /**
     * Provides an instance of this object cast to the given class.
     * This is done by temporarily transforming the object's header
     * to the given type to allow it to be cast successfully.
     *
     * @param object the object to cast
     * @param cls    the target class
     * @param <T>    the target type
     * @return the object, cast to the target type
     * @deprecated This doesn't actually work - the generic isn't a hard type :(
     * Working on a replacement.
     */
    @Contract(value = "null, _ -> null")
    @Deprecated
    public static <T> T carbonCast(Object object, Class<T> cls) {
        if (object == null) return null;
        final Class<?> original = object.getClass();
        try {
            return transform(object, cls);
        } finally {

            transform(object, original);
        }
    }

    /**
     * Useless in most situations.
     *
     * @return the class this method was called in.
     */
    public static Class<?> whereAmI() {
        final StackTraceElement[] trace = createStackTrace();
        try {
            return Class.forName(trace[1].getClassName());
        } catch (Throwable e) { // Can be thrown in bad case
            return Overlord.class;
        }
    }

    /**
     * Generates a stack trace and reduces it to the caller-location.
     *
     * @return a new stacktrace
     */
    public static StackTraceElement[] createStackTrace() {
        try {
            final StackTraceElement[] trace = new Throwable().getStackTrace();
            final StackTraceElement[] elements = new StackTraceElement[trace.length - 1];
            System.arraycopy(trace, 1, elements, 0, elements.length);
            return elements;
        } catch (Throwable ex) {
            final StackTraceElement[] trace = ex.getStackTrace();
            final StackTraceElement[] elements = new StackTraceElement[trace.length - 2];
            System.arraycopy(trace, 2, elements, 0, elements.length);
            return elements;
        }
    }

    /**
     * This could be very useful to prevent access from external classes
     * or to track object creation, etc.
     * <p>
     * Also very helpful for debugging.
     *
     * @return the class the current method was called from.
     */
    public static Class<?> whereWasI() {
        final StackTraceElement[] trace = createStackTrace();
        try {
            return Class.forName(trace[2].getClassName());
        } catch (Throwable e) { // Can be thrown in bad case
            return Overlord.class;
        }
    }

    /**
     * Attempts to add the given classes as superclasses to
     * the target class.
     * <p>
     * VERY unsafe if misused.
     *
     * @param target       the target class
     * @param superClasses the new superclasses
     * @deprecated Unsafe, likely to break.
     */
    @Deprecated
    public static void addSuperclass(Class<?> target, Class<?>... superClasses) {
        if (IS_COMPRESSED_OOP) throw new RuntimeException("This does not work safely in 64-bit java.");
        final long offset = 8L;
        final long address = unsign(UNSAFE.getInt(createEmpty(target), offset));
        for (Class<?> cls : superClasses) {
            final long superAddress = unsign(UNSAFE.getInt(createEmpty(cls), offset));
            UNSAFE.putAddress(address + 36, superAddress);
        }
    }

    public static long unsign(int value) {
        if (value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    /**
     * Attempts to safely define a non-anonymous class.
     * The name will be taken from the bytecode and use
     * the caller package.
     *
     * @param bytecode the class bytecode (from a .class file)
     * @return the class, not yet class-loaded
     */
    public static Class<?> defineClassSafely(byte[] bytecode) {
        try {
            Object definer = METHODS[3].invoke(MethodHandles.lookup(), bytecode);
            Class<?> definerClass = definer.getClass();
            Method method = definerClass.getDeclaredMethod("defineClass", boolean.class);
            return (Class<?>) method.invoke(definer, false);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            try {
                return MethodHandles.lookup().defineClass(bytecode);
            } catch (Throwable ex) {
                ex.addSuppressed(e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Attempts to define a non-anonymous class while ignoring all
     * security managers.
     *
     * @param name     the class name
     * @param bytecode the class bytecode (from a .class file)
     * @return the class, not yet class-loaded
     */
    public static Class<?> defineClass(String name, byte[] bytecode) {
        return defineClass0(name, bytecode, 0, bytecode.length, Overlord.class.getClassLoader(), Overlord.class.getProtectionDomain());
    }

    static Class<?> defineClass0(String name, byte[] bytecode, int offset, int length, ClassLoader loader, ProtectionDomain domain) {
        try {
            return (Class<?>) METHODS[1].invoke(JDK_UNSAFE, name, bytecode, offset, length, loader, domain);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Defines a new anonymous class inside the given class.
     *
     * @param host             the enclosing class
     * @param bytecode         the class bytecode
     * @param classPathPatches the CP patches
     */
    public static void defineAnonymousClass(Class<?> host, byte[] bytecode, Object[] classPathPatches) {
        UNSAFE.defineAnonymousClass(host, bytecode, classPathPatches);
    }

    /**
     * Verifies whether the given class can be accessed.
     * This is done WITHOUT loading the class.
     * <p>
     * The &lt;clinit&gt; is not called.
     *
     * @param cls the class to check
     * @return true if it can be accessed
     */
    public static boolean canAccess(Class<?> cls) {
        try {
            MethodHandles.lookup().accessClass(cls);
            return true;
        } catch (IllegalAccessException ex) {
            return false;
        }
    }

    /**
     * Creates a deep clone of an object. This means that
     * any non-constable fields will be deep clones of their
     * original objects.
     *
     * @param source the source object
     * @param <T>    the type
     * @return the new clone
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T source) {
        if (source == null) return null;
        T destination = (T) createEmpty(source.getClass());
        deepCopy0(source, destination);
        return destination;
    }

    /**
     * Copies over all data from the source to the target.
     * The source and target must have the same data layout
     * in memory for this to work correctly (ideally should
     * be the same class, or at least have the same field
     * erasure.)
     * <p>
     * Any field data in the result will be deeply-cloned
     * from the source.
     *
     * @param source the source object
     * @param target the target object
     * @param <T>    the type
     */
    public static <T> void deepCopy(T source, T target) {
        assert source != null;
        assert target != null;
        assert getMemorySize0(source) == getMemorySize0(target);
        deepCopy0(source, target);
    }

    static void deepCopy0(Object source, Object destination) {
        allowAccess(destination.getClass());
        final long sourcePointer, destinationPointer;
        final long length = getMemorySize0(source);
        sourcePointer = getAddress(source);
        destinationPointer = getAddress(destination);
        UNSAFE.copyMemory(sourcePointer, destinationPointer, length);
        for (final Field field : destination.getClass().getDeclaredFields()) {
            try {
                if (field.getType().isPrimitive()) continue;
                if (!field.isAccessible()) field.setAccessible(true);
                allowAccess(field.getType());
                final Object a = field.get(source);
                if (a instanceof Constable) continue;
                if (a == null) continue;
                final Object b = shallowClone(a);
                deepCopy0(a, b);
                field.set(destination, b);
            } catch (IllegalAccessException e) {
                // Ignore - our shallow copy is probably fine.
            }
        }
    }

    /**
     * Creates a surface clone of an object. This means that
     * any non-constable fields will copy their references
     * from the original object.
     *
     * @param source the source object
     * @param <T>    the type
     * @return the new clone
     */
    @SuppressWarnings("unchecked")
    public static <T> T shallowClone(T source) {
        if (source == null) return null;
        T destination = (T) createEmpty(source.getClass());
        shallowCopy0(source, destination);
        return destination;
    }

    /**
     * Copies over all data from the source to the target.
     * The source and target must have the same data layout
     * in memory for this to work correctly (ideally should
     * be the same class, or at least have the same field
     * erasure.)
     * <p>
     * Any field data will be shallow-cloned from the source.
     *
     * @param source the source object
     * @param target the target object
     * @param <T>    the type
     */
    public static <T> void shallowCopy(T source, T target) {
        assert source != null;
        assert target != null;
        assert getMemorySize0(source) == getMemorySize0(target);
        shallowCopy0(source, target);
    }
    
    static void shallowCopy0(Object source, Object destination) {
        final long sourcePointer, destinationPointer;
        final long length = getMemorySize0(source);
        sourcePointer = getAddress(source);
        destinationPointer = getAddress(destination);
        UNSAFE.copyMemory(sourcePointer, destinationPointer, length);
    }
    
    @Deprecated
    public static <T> Constructor<T> createConstructor() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (METHODS[4] == null) throw new IllegalStateException("ReflectAccess is not available in this environment.");
        return null;
    }
    
    /**
     * Returns the class definition/data, which is transient and
     * assigned by the JVM at runtime.
     *
     * This is the same as the gunk that defineClass spits back.
     * For most classes, it will be null.
     *
     * @param cls the class
     * @return the class data/def, provided by JLA
     */
    public static @Nullable Object getClassDefinition(final Class<?> cls) {
        try {
            final Object jLA = javaLangAccess();
            if (METHODS[11] == null) {
                Overlord.breakEncapsulation(jLA.getClass(), true);
                Overlord.allowAccess(jLA.getClass(), true);
                final Class<?> klass = Class.forName("java.lang.System$2");
                Overlord.breakEncapsulation(klass, true);
                Overlord.allowAccess(klass, true);
                METHODS[11] = jLA.getClass().getDeclaredMethod("classData", Class.class);
                METHODS[11].setAccessible(true);
            }
            return METHODS[11].invoke(jLA, cls);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            UNSAFE.throwException(e);
            return null;
        }
    }
    
    static void breakEncapsulation(Class<?> target, boolean accessNamedModules) {
        if (!accessNamedModules) breakEncapsulation(target);
        else {
            try {
                METHODS[5].invoke(target.getModule(),
                    target.getPackageName(),
                    Overlord.class.getModule(),
                    false,
                    true);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This is JLA. It gives you access to some cool internal stuff.
     * Java is a filthy capitalist and likes to hoard all the cool stuff.
     *
     * The proletariat masses may now rise up and mess with it. :D
     *
     * Returns the value from OxfordSecrets <- SharedSecrets.
     *
     * If anybody gets that reference, let me know and I'll give you a reward.
     * @param <T> In case you have JLT in your classpath, you can cast this to the actual class
     * @return JLA
     */
    public static <T> T javaLangAccess() {
        return (T) OxfordSecrets.javaLangAccess;
    }
    
    static void allowAccess(Class<?> target, boolean accessNamedModules) {
        if (!accessNamedModules) allowAccess(target);
        else {
            try {
                METHODS[5].invoke(target.getModule(),
                    target.getPackageName(),
                    Overlord.class.getModule(),
                    true,
                    true);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    static void breakEncapsulation(Class<?> target) {
        target.getModule().addExports(target.getPackageName(), Overlord.class.getModule());
    }
    
    /**
     * ReflectAccess gives you the ability to mess with even cooler stuff than JLA.
     *
     * @param <T> In case you have JLT in your classpath, you can cast this to the actual class
     * @return ReflectAccess instance
     */
    public static <T> T javaLangReflectAccess() {
        return (T) reflectAccess;
    }
    
    /**
     * Returns the call behaviour used by methods.
     * This is runtime-generated from the bytecode, and may be replaced by JIT later on.
     *
     * This will probably be null - JVM is very lazy about filling this in unless
     * it actually has to.
     *
     * @param method the method
     * @return the behaviour
     */
    public static @Nullable MethodBehaviour getReflectiveBehaviour(final Method method) {
        try {
            Object object = METHODS[6].invoke(reflectAccess, method);
            return new Delegate(object);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            UNSAFE.throwException(e);
            return null;
        }
    }
    
    /**
     * Allows you to replace the behaviour of a method when it's accessed via reflection.
     *
     * This allows you to actually change how a method works based on whether it's called using
     * reflection or normally. See RewriteBehaviourTest.
     *
     * Note: you CANNOT change the normative behaviour!!!
     * JVM caches this in a delegate at clinit and there is no way to navigate the tree to
     * find and replace it. JIT will also replace it later even if you access it via the stack.
     *
     * @param method
     * @param behaviour
     * @param <T>
     */
    public static <T extends MethodBehaviour> void setReflectiveBehaviour(final Method method, T behaviour) {
        try {
            Object invoker = Proxy.newProxyInstance(Overlord.class.getClassLoader(), new Class[]{
                methodAccessorClass, MethodBehaviour.class
            }, (proxy, none, args) -> behaviour.invoke(proxy, args));
            METHODS[7].invoke(reflectAccess, ensureRoot(method), invoker);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            UNSAFE.throwException(e);
        }
    }
    
    static void parkThread(long epochMilliStamp) {
        UNSAFE.park(true, epochMilliStamp);
    }
    
    /**
     * Ensures the root method.
     * JDK uses a tree-leaf access tree to make sure you never get the actual root method.
     * This navigates up the tree until it reaches the root.
     * @param method the method
     * @return the root of that method
     */
    public static Method ensureRoot(Method method) {
        try {
            while (METHODS[9].invoke(method) instanceof Method root)
                method = root;
        } catch (Throwable ex) {
            UNSAFE.throwException(ex);
        }
        return method;
    }
    
    static void loadFence() {
        UNSAFE.loadFence();
    }
    
    /**
     * This might not be accurate - nobody knows the offsets for 64-bit Java.
     * Trust me, I contacted the Hotspot contributor who wrote the code, even he doesn't know it.
     *
     * @param cls the class
     * @return the address of the class
     */
    @Deprecated
    public static long getInternalAddress(Class<?> cls) {
        if (IS_COMPRESSED_OOP)
            return UNSAFE.getInt(cls, 84L);
        else return UNSAFE.getLong(cls, 160L);
    }
    
    static void allowAccess(Class<?> target) {
        target.getModule().addOpens(target.getPackageName(), Overlord.class.getModule());
    }
    
    public static Method getRootMethod(Class<?> cls, String name, Class<?>... parameterTypes) throws InvocationTargetException, IllegalAccessException {
        return (Method) METHODS[10].invoke(cls, name, parameterTypes);
    }
    
    static Object getMethodAccessor(final Method method) throws InvocationTargetException, IllegalAccessException {
        return METHODS[6].invoke(reflectAccess, ensureRoot(method));
    }
    
    static void unparkThread(final Thread thread) {
        UNSAFE.unpark(thread);
    }
    
    /**
     * Opens a class to your module.
     * This bypasses the security-manager check as well.
     * If the class is in jdk.internal, you'll want to break encapsulation on it first.
     *
     * @param accessor the accessor class
     * @param target the target class
     * @param accessNamedModules true if it's in jdk.internal, otherwise false
     */
    public static void allowAccess(final Class<?> accessor, final Class<?> target, final boolean accessNamedModules) {
        if (!accessNamedModules) target.getModule().addOpens(target.getPackageName(), accessor.getModule());
        else {
            try {
                METHODS[5].invoke(target.getModule(),
                    target.getPackageName(),
                    accessor.getModule(),
                    true,
                    true);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Breaks the encapsulation of a java.base-only class.
     * Adds your module to the exports.
     * You can then open it using {@link #allowAccess(Class, Class, boolean)}
     *
     * This allows you to access jdk.internal things without special commandline args or all that.
     *
     * @param accessor the accessor class
     * @param target the target class
     * @param accessNamedModules true if it's in jdk.internal, otherwise false
     */
    public static void breakEncapsulation(final Class<?> accessor, final Class<?> target, final boolean accessNamedModules) {
        if (!accessNamedModules) target.getModule().addExports(target.getPackageName(), accessor.getModule());
        else {
            try {
                METHODS[5].invoke(target.getModule(),
                    target.getPackageName(),
                    accessor.getModule(),
                    false,
                    true);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
}
