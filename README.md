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
