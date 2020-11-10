Overlord
=====

A powerful memory management library.

Perform illegal, unsafe and incredibly dangerous operations on your JVM's native memory with no restrictions.

You can also pretend this is C, and manage memory directly! :)

Features:
  * Create objects without using their constructor.
  * Edit objects in heap memory.
  * Assign and manage memory outside of the heap.
  * Store objects and data outside of the heap.
  * Edit intrinsic parts of an object, such as headers, klass pointers, mark words, etc.
  * Transform objects to an incompatible type at runtime.
  * Cast objects to incompatible types.
  * Create shallow and deep perfect clones of objects.
  * Construct objects using the constructor from a completely unrelated class.
  * Trace where methods are called.
  * Use real objects to provide the implementation for native methods.
  * Load classes at runtime, both hidden and explicitly.
  * Create pointers to objects in memory.
 

### Maven Information
```xml
<repository>
    <id>pan-repo</id>
    <name>Pandaemonium Repository</name>
    <url>https://gitlab.com/api/v4/projects/18568066/packages/maven</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>overlord</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Examples

Shallow-clone an object (creates a NEW instance, but any non-primitive field will be a reference to the original.)
```java 
final MyObject original = new MyObject();
final MyObject clone = Overlord.shallowClone(source);

assert original.objectField == clone.objectField;
```

Deep-clone an object (creates a NEW instance, any non-primitive non-constant field will be recursively deep-cloned from the original.)
```java 
final MyObject original = new MyObject();
final MyObject clone = Overlord.deepClone(source);

assert original.objectField != clone.objectField;
```

With the followinc class structure:
```java
static class NativeImplClass {
    public native String getWord();
    public native void setWord(String word);
    public native int getNumber();
    public native void setNumber(int number);
}

static class RealClass {
    int number = 10;
    String word = "hello";

    protected String getWord() { return word; }
    protected void setWord(String word) { this.word = word; }
    protected int getNumber() { return number; }
    protected void setNumber(int number) { this.number = number; }
}
```

You can use `NativeImplClass` as a blind implementation in the following:
```java 
final RealClass original = new RealClass();
final NativeImplClass cast = Overlord.transform(original, NativeImplClass.class); // Transforms 'original' to an instance of NativeImplClass
// the 'cast' variable is strongly-typed and so valid hereafter
Overlord.transform(original, RealClass.class); // Transforms 'original' back to its true class
// Now any methods called will be executed on RealClass

cast.setNumber(10); // The compiler will execute this public method from NativeImplClass
                    // The JVM will *actually* execute the protected getNumber method from RealClass
```

Swapping a constructor:
```java
static class Class2 {
    public final int number;
    public final String name;

    public Class2(String string) {
        number = 10;
        name = string;
    }

}

static class Class1 {
    public final int number;
    public final String name;

    public Class1(String string) {
        number = 5;
        name = string + " <- thing";
    }

}
```

Any fields set in the copy constructor will either target the object's fields or be ignored silently.
```java 
final Class1 class1 = Overlord.createSwapConstructor(Class1.class, Class2.class, "hello");

assert class1.number == 10;
assert class1.name.equalsIgnoreCase("hello");
```

Creating an empty object:
```java 
final Class1 obj = Overlord.createEmpty(Class1.class);
```