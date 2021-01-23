package mx.kenzie.overlord;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface MethodBehaviour {
    
    Object invoke(Object obj, Object... args)
        throws IllegalArgumentException, InvocationTargetException;
    
    class Impl implements MethodBehaviour {
        
        public native Object invoke(Object obj, Object... args)
            throws IllegalArgumentException, InvocationTargetException;
        
    }
    
}

record Delegate(Object accessor) implements MethodBehaviour {
    
    @Override
    public Object invoke(Object obj, Object... args) throws IllegalArgumentException, InvocationTargetException {
        try {
            return Overlord.METHODS[8].invoke(accessor, obj, args);
        } catch (IllegalAccessException e) {
            Overlord.UNSAFE.throwException(e);
            return null;
        }
    }
    
}
