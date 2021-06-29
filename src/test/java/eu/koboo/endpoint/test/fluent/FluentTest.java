package eu.koboo.endpoint.test.fluent;

import eu.koboo.endpoint.client.ClientBuilder;
import eu.koboo.endpoint.client.FluentClient;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.endpoint.server.ServerBuilder;
import eu.koboo.endpoint.test.TestConstants;
import eu.koboo.endpoint.test.TestRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class FluentTest {

    public static EndpointServer server;
    public static FluentClient client;

    @BeforeClass
    public static void setupClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void test() throws Exception {
        System.out.println("== Init-Sequence == ");
        System.out.println("== 'FluentTest' == ");

        System.out.println("== Endpoint-Build == ");

        EndpointBuilder builder = TestConstants.BUILDER.logging(false);

        System.out.println("== Server-Build == ");

        server = ServerBuilder.fluentOf(builder)
                .changePort(54321)
                .onConnect(c -> System.out.println("Server connected! " + c.toString()))
                .onDisconnect(c -> System.out.println("Server disconnected! " + c.toString()))
                .onStart(() -> System.out.println("Server started!"))
                .onStop(() -> System.out.println("Server stopped!"))
                .onPacket(TestRequest.class, (c, p) -> {
                    TestConstants.assertRequest(p);
                    c.writeAndFlush(p).syncUninterruptibly();
                    System.out.println("Server received! " + c.toString());
                })
                .bind();

        System.out.println("== Client-Build == ");

        client = ClientBuilder.fluentOf(builder)
                .changeAddress("localhost", 54321)
                .onConnect(() -> {
                    System.out.println("Client connected!");
                    client.send(TestConstants.TEST_REQUEST);
                })
                .onDisconnect(() -> System.out.println("Client disconnected!"))
                .onStart(() -> System.out.println("Client started!"))
                .onStop(() -> System.out.println("Client stopped!"))
                .onError((c, t) -> System.out.println("Client error: " + c.getSimpleName() + "/" + t.getClass().getSimpleName()))
                .onPacket(TestRequest.class, p -> {
                    System.out.println("Client received!");
                    TestConstants.assertRequest(p);

                    System.out.println("== Stop-Sequence == ");
                    assertTrue(client.stop());
                    assertTrue(server.stop());
                })
                .connect();

        Thread.sleep(20_000);
    }

}
