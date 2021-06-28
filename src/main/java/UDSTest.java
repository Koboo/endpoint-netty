import eu.koboo.endpoint.client.ClientBuilder;
import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.channel.ChannelAction;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.core.util.BufUtils;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.endpoint.server.ServerBuilder;
import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

public class UDSTest {
    /*

    This class was not written as unit-test, because it is compiled and executed directly on a Linux machine.

     */

    public static void main(String[] args) throws InterruptedException {
        EndpointBuilder builder = EndpointBuilder.builder()
                .errorMode(ErrorMode.STACK_TRACE)
                .logging(true);
        System.out.println("Setting up builder..");

        System.out.println("Creating universal EventListener..");

        Consumer<ChannelActionEvent> eventConsumer = channelActionEvent -> {
            ChannelAction channelAction = channelActionEvent.getAction();
            if(channelAction == null) {
                System.out.println("action is null.");
                return;
            }
            System.out.println("Action: " + channelAction.name());
        };

        System.out.println("Initialize server..");
        EndpointServer server = ServerBuilder.of(builder, 666);
        System.out.println("Registering listener (server)..");
        server.registerEvent(ChannelActionEvent.class, eventConsumer);
        server.registerEvent(ReceiveEvent.class, event -> {
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
        EndpointClient client = ClientBuilder.of(builder, "localhost", 666);
        System.out.println("Registering listener (client)..");
        client.registerEvent(ChannelActionEvent.class, eventConsumer);
        System.out.println("Starting client..");
        client.start();

        System.out.println("Started client/server..");
        Thread.sleep(2500);

        System.out.println("Sending...");
        client.send(new UDSPacket("Striiiiing", -5000, 123456789)).sync();
        System.out.println("Sent!");
    }

    public static class UDSPacket implements EndpointPacket {

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

        @Override
        public void read(ByteBuf byteBuf) {
            this.testString = BufUtils.readString(byteBuf);
            this.testLong = BufUtils.readVarLong(byteBuf);
            this.testInt = BufUtils.readVarInt(byteBuf);
        }

        @Override
        public void write(ByteBuf byteBuf) {
            BufUtils.writeString(testString, byteBuf);
            BufUtils.writeVarLong(testLong, byteBuf);
            BufUtils.writeVarInt(testInt, byteBuf);
        }
    }

}
