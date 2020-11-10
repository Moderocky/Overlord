package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;
import org.junit.Test;

public class NativeImplementationTest {

    @Test
    public void main() {
        final RealClass original = new RealClass();

        final NativeImplClass cast = cast(original);

        assert cast.getNumber() == 10;
        assert cast.getWord().equals("hello");
        cast.setNumber(6);
        cast.setWord("there");
        assert cast.getNumber() == 6;
        assert cast.getWord().equals("there");

        assert cast == (Object) original;
    }

    private NativeImplClass cast(RealClass obj) {
        final NativeImplClass object = Overlord.transform(obj, NativeImplClass.class);
        Overlord.transform(obj, RealClass.class);
        return object;
    }

    @Test
    public void entTest() {
        final NMSEntity original = new NMSEntity();
        final Entity cast = cast(original);

        assert cast.a() == original.a();
        assert cast.b() == original.b();
        assert cast == (Object) original;
    }

    private Entity cast(NMSEntity obj) {
        final Entity object = Overlord.transform(obj, Entity.class);
        Overlord.transform(obj, NMSEntity.class);
        return object;
    }

    static class NativeImplClass {

        public native String getWord();

        public native void setWord(String word);

        public native int getNumber();

        public native void setNumber(int number);

    }

    static class RealClass {

        int number = 10;
        String word = "hello";

        protected String getWord() {
            return word;
        }

        protected void setWord(String word) {
            this.word = word;
        }

        protected int getNumber() {
            return number;
        }

        protected void setNumber(int number) {
            this.number = number;
        }

    }

    static class Entity {
        public native int a();

        public native int b();

        public native void qQ(double d0);
    }

    static class NMSEntity {

        public int a() {
            return 0;
        }

        public int b() {
            return 1;
        }

        public void qQ(double d0) {
        }

    }


}
