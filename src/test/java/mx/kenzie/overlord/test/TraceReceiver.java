package mx.kenzie.overlord.test;

import mx.kenzie.overlord.Overlord;

public class TraceReceiver {

    static void receiveTraceTest() {
        assert Overlord.whereAmI().equals(TraceReceiver.class);
        assert Overlord.whereWasI().equals(TraceTest.class);
    }

}
