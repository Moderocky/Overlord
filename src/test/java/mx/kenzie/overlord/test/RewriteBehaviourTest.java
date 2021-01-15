package mx.kenzie.overlord.test;

import mx.kenzie.overlord.MethodBehaviour;
import mx.kenzie.overlord.Overlord;
import org.junit.Test;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

public class RewriteBehaviourTest {
    
    @Test
    public void alterRootMethod() throws Throwable {
        Object access = Overlord.javaLangReflectAccess();
        assert access != null;
        
        final Blob blob = new Blob();
        Method method = Overlord.getRootMethod(Blob.class, "test");
        {
            Method method2 = Overlord.getRootMethod(Blob.class, "test");
            assert method == method2;
        }

        MethodBehaviour replacement = (obj, args) -> "bar";

        Overlord.setReflectiveBehaviour(method, replacement);
        assert method.invoke(blob).equals("bar");
        assert blob.test().equals("foo");
        {
            Method method2 = Overlord.getRootMethod(Blob.class, "test");
            assert method2.invoke(blob).equals("bar");
            assert method == method2;
        }
        
    }
    
    @Test
    public void changeReflectiveAccess() throws Throwable {
        {
            Method method = Blob.class.getMethod("myMethod");
            Overlord.setReflectiveBehaviour(method, (obj, args) -> "please don't use reflection on me!");
        }
        
        Method method = Blob.class.getMethod("myMethod");
        
        assert !method.invoke(new Blob()).equals(new Blob().myMethod());
    }
    
    @Test
    public void attemptNormativeRewrite() throws Throwable {
        {
            Method method = Beans.class.getMethod("myMethod");
            Overlord.setReflectiveBehaviour(Overlord.ensureRoot(method), (obj, args) -> "foo");
        }
        final Beans beans = new Beans();
        Method method = Beans.class.getMethod("myMethod");
    
        assert method.invoke(beans).equals("foo");
        UnsatisfiedLinkError error = null;
        try {
            System.out.println(beans.myMethod());
        } catch (UnsatisfiedLinkError e) {
            error = e;
        }
        assert error != null;
        
        final Box box = Overlord.transform(beans, Box.class);
    
        assert beans.myMethod().equals("hi");
        assert method.invoke(beans).equals("foo");
        
        assert box instanceof Box;
        assert (Object) beans instanceof Box;
        assert box == (Object) beans;
        
    }
    
    private static class Blob {
        
        public String myMethod() {
            return "thank you for accessing me properly";
        }
    
        public String test() {
            return "foo";
        }
        
    }
    
    private static class Beans {
        
        public native String myMethod();
        
    }
    
    private static class Box {
        
        public String myMethod() {
            return "hi";
        }
        
    }
    
}
