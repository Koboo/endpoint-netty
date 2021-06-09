import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.builder.param.EventMode;
import eu.koboo.endpoint.core.codec.serial.SerializableCodec;
import eu.koboo.endpoint.core.codec.serial.SerializablePacket;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.server.EndpointServer;

import java.util.function.Consumer;

public class UDSTest {

    public static void main(String[] args) throws InterruptedException {
        EndpointBuilder builder = EndpointBuilder.newBuilder()
                .codec(SerializableCodec.class)
                .errorMode(ErrorMode.STACK_TRACE)
                .eventMode(EventMode.SYNC)
                .logging(true);
        System.out.println("Setting up builder..");

        System.out.println("Creating universal EventListener..");

        Consumer<ChannelActionEvent> eventConsumer = channelActionEvent -> {
            ChannelActionEvent.Action action = channelActionEvent.getAction();
            if(action == null) {
                System.out.println("action is null.");
                return;
            }
            System.out.println("Action: " + action.name());
        };

        System.out.println("Initialize server..");
        EndpointServer server = new EndpointServer(builder, 666);
        System.out.println("Registering listener (server)..");
        server.eventHandler().register(ChannelActionEvent.class, eventConsumer);
        server.eventHandler().register(ReceiveEvent.class, event -> {
            if(event.getObject() instanceof UDSPacket) {
                UDSPacket udsPacket = (UDSPacket) event.getTypeObject();
                System.out.println("String: " + udsPacket.getTestString());
                System.out.println("Long: " + udsPacket.getTestLong());
                System.out.println("Int: " + udsPacket.getTestInt());
            }
        });
        System.out.println("Starting server..");
        server.start();

        System.out.println("Initialize client..");
        EndpointClient client = new EndpointClient(builder, "localhost", 666);
        System.out.println("Registering listener (client)..");
        client.eventHandler().register(ChannelActionEvent.class, eventConsumer);
        System.out.println("Starting client..");
        client.start();

        System.out.println("Started client/server..");
        Thread.sleep(2500);

        System.out.println("Sending...");
        client.send(new UDSPacket("Striiiiing", -5000, 123456789), true);
        System.out.println("Sent!");
    }

    public static class UDSPacket implements SerializablePacket {

        String testString;
        long testLong;
        int testInt;

        public UDSPacket(String testString, long testLong, int testInt) {
            this.testString = testString;
            this.testLong = testLong;
            this.testInt = testInt;
        }

        public String getTestString() {
            return testString;
        }

        public long getTestLong() {
            return testLong;
        }

        public int getTestInt() {
            return testInt;
        }
    }

}
