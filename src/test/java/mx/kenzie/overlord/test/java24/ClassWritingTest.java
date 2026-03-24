package mx.kenzie.overlord.test.java24;

import mx.kenzie.overlord.Overlord;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;

public class ClassWritingTest {
    @Test
    public void hiddenClassTest() throws IOException {
        final ClassFile classFile = ClassFile.of();
        final ClassModel model;
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(Hidden.class.getName().replace('.', '/') + ".class")) {
            assert in != null;
            model = classFile.parse(in.readAllBytes());
        }

        final byte[] bytes = classFile.transformClass(model, ClassDesc.of(Object.class.getPackageName(), Hidden.class.getSimpleName()), ClassTransform.ACCEPT_ALL);

        Overlord.defineAnonymousClass(Object.class, bytes);
        assert "true:java.lang.Object".equals(System.getProperty("hidden.test"));
    }

    static class Hidden {
        static {
            final Class<?> self = MethodHandles.lookup().lookupClass();
            final String value = self.isHidden() + ":" + self.getNestHost().getName();
            System.setProperty("hidden.test", value);
        }
    }
}
