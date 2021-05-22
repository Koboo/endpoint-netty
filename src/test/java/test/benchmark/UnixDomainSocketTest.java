package test.benchmark;

import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.builder.param.EventMode;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.core.protocols.serializable.SerializablePacket;
import eu.koboo.endpoint.core.protocols.serializable.SerializableReceiveEvent;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.event.listener.EventListener;
import io.netty.channel.Channel;
import io.netty.channel.unix.DomainSocketChannel;

public class UnixDomainSocketTest {

    public static void main(String[] args) throws InterruptedException {
        EndpointBuilder builder = EndpointBuilder.newBuilder()
                .protocol(Protocol.SERIALIZABLE)
                .errorMode(ErrorMode.STACK_TRACE)
                .eventMode(EventMode.SYNC)
                .logging(false)
                .isUsingUDS("/tmp/test.sock");
        System.out.println("Setting up builder..");

        System.out.println("Creating universal EventListener..");
        EventListener<ChannelActionEvent> listener = new EventListener<ChannelActionEvent>() {
            @Override
            public void onEvent(ChannelActionEvent channelActionEvent) {
                ChannelActionEvent.Action action = channelActionEvent.getAction();
                Channel channel = channelActionEvent.getChannel();
                if(action == null) {
                    System.out.println("action is null.");
                    return;
                }
                System.out.println("Action: " + action.name());
                if(channel == null) {
                    System.out.println("channel is null.");
                    return;
                }
                if(channel instanceof DomainSocketChannel) {
                    System.out.println("using uds!");
                }
            }
        };
        EventListener<SerializableReceiveEvent> receive = new EventListener<SerializableReceiveEvent>() {
            @Override
            public void onEvent(SerializableReceiveEvent event) {
                if(event.getTypeObject() instanceof UDSPacket) {
                    UDSPacket udsPacket = (UDSPacket) event.getTypeObject();
                    System.out.println("String: " + udsPacket.getTestString());
                    System.out.println("Long: " + udsPacket.getTestLong());
                    System.out.println("Int: " + udsPacket.getTestInt());
                }
            }
        };

        System.out.println("Initialize server..");
        EndpointServer server = new EndpointServer(builder);
        System.out.println("Registering listener (server)..");
        server.eventHandler().register(listener);
        server.eventHandler().register(receive);
        System.out.println("Starting server..");
        server.start();

        System.out.println("Initialize client..");
        EndpointClient client = new EndpointClient(builder);
        System.out.println("Registering listener (client)..");
        client.eventHandler().register(listener);
        System.out.println("Starting client..");
        client.start();

        Thread.sleep(2500);

        client.send(new UDSPacket("Striiiiing", -5000, 123456789));
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
