package chill.rpc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RPCTest {

    @BeforeAll
    public static void registerHandler(){
        var service = RPC.implement(SampleInterface.class).with(new SampleImpl());
    }

    @Test
    public void bootstrap() {
        SampleInterface rpcInterface = RPC.make(SampleInterface.class);

        int oneAdded = rpcInterface.addOne(41);

        assertEquals(42, oneAdded);

    }

    @Test
    public void list() {
        SampleInterface rpcInterface = RPC.make(SampleInterface.class);

        List<String> filtered = rpcInterface.filter(List.of("a", "b", "c"), "a");

        assertEquals(List.of("a"), filtered);
    }

    @Test
    public void bean() {
        SampleInterface rpcInterface = RPC.make(SampleInterface.class);
        SampleBean sampleBean = new SampleBean();
        sampleBean.setName("foo");

        assertEquals("foo", sampleBean.getName());

        sampleBean = rpcInterface.updateBeansName(sampleBean);

        assertEquals("bar", sampleBean.getName());

    }

    @Test
    public void async() throws InterruptedException {
        SampleInterface rpcInterface = RPC.make(SampleInterface.class);
        SampleBean sampleBean = new SampleBean();
        sampleBean.setName("foo");

        assertEquals("foo", sampleBean.getName());

        Thread otherThread =
                RPC.async(() -> rpcInterface.updateBeansName(sampleBean),
                        (result) -> assertEquals("bar", result.getName()));

        otherThread.join();
    }

    @Test
    public void exception() {

        SampleInterface rpcInterface = RPC.make(SampleInterface.class);
        try {
            rpcInterface.throwsAnException();
            fail("Should have thrown");
        } catch (Exception e) {
            assertEquals("Foo", e.getMessage());
            assertEquals(SampleRuntimeException.class, e.getClass());
        }
    }

    @Test
    public void mock() {
        SampleInterface rpcInterface = RPC.make(SampleInterface.class);
        try {
            RPC.registerMock(SampleInterface.class, new SampleImpl(){
                @Override
                public void throwsAnException() {
                    // do not throw!
                }
            });

            // mock should prevent throwing here
            rpcInterface.throwsAnException();
        } finally {
            RPC.deRegisterMock(SampleInterface.class);
        }
    }


}
