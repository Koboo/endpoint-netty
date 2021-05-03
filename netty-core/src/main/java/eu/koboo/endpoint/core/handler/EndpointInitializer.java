package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.protocols.natives.NativeCodec;
import eu.koboo.endpoint.core.protocols.serializable.SerializableCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class EndpointInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint endpoint;
    private final EndpointHandler endpointHandler;
    private final EventExecutorGroup executorGroup;

    public EndpointInitializer(Endpoint endpoint) {
        this.endpoint = endpoint;

        this.endpointHandler = new EndpointHandler(endpoint);

        int cores = Runtime.getRuntime().availableProcessors();

        this.executorGroup = new DefaultEventExecutorGroup(cores * 4);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // Get pipeline-instance
        ChannelPipeline pipeline = ch.pipeline();

         //Add logging-handler if enabled
         if (endpoint.builder().isLogging())
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));

        pipeline.addLast(endpoint.builder().getCompression().getEncoder());
        pipeline.addLast(endpoint.builder().getCompression().getDecoder());

        if (endpoint.isClient()) {
            pipeline.addLast("idle-state", new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
            pipeline.addLast("idle-handler", new EndpointIdleHandler(endpoint));
        }

        // Add the ByteToMessageCodec<Object>
        if (endpoint.builder().getProtocol() == Protocol.NATIVE)
            pipeline.addLast("packet-codec", new NativeCodec(endpoint));
        else
            pipeline.addLast("packet-codec", new SerializableCodec(endpoint));

        // Add the business-logic handler with async executor.
        pipeline.addLast(executorGroup, "netty-handler", this.endpointHandler);
    }
}
