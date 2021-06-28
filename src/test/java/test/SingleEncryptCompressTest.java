package test;

import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.util.Compression;
import eu.koboo.endpoint.server.EndpointServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SingleEncryptCompressTest {

    public static EndpointServer server;
    public static EndpointClient client;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test SingleRequest/Sec Behaviour == ");

        TestConstants.BUILDER
                .logging(true)
                .password("ThisIsMyStrongPassword")
                .compression(Compression.SNAPPY);

        server = new EndpointServer(TestConstants.BUILDER, 54321);
        server.eventHandler().register(ReceiveEvent.class, event -> {
            if (event.getTypeObject() instanceof TestRequest) {
                TestRequest request = event.getTypeObject();

                assertEquals(request.getTestString(), TestConstants.testString);
                assertEquals(request.getTestLong(), TestConstants.testLong);
                assertArrayEquals(request.getTestBytes(), TestConstants.testBytes);

                System.out.println("Received Request!");
            }
        });

        client = new EndpointClient(TestConstants.BUILDER, "localhost", 54321);

        assertTrue(server.start());
        assertTrue(client.start());

    }

    @AfterClass
    public static void afterClass() {
        assertTrue(client.stop());
        assertTrue(server.stop());
        System.out.println("== Finished SingleRequest/Sec Behaviour == ");
    }

    @Test
    public void testPerSec() throws Exception {
        Thread.sleep(250);
        client.send(TestConstants.TEST_REQUEST);
    }

}
