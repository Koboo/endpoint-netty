package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.protocols.natives.NativeCodec;
import eu.koboo.endpoint.core.protocols.serializable.SerializableCodec;
import eu.koboo.nettyutils.Compression;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class EndpointInitializer extends ChannelInitializer<Channel> {

    private final Endpoint endpoint;
    private final ChannelGroup channels;
    private final EventExecutorGroup executorGroup;

    public EndpointInitializer(Endpoint endpoint, ChannelGroup channels) {
        this.endpoint = endpoint;
        this.channels = channels;
        int cores = Runtime.getRuntime().availableProcessors();
        this.executorGroup = new DefaultEventExecutorGroup(cores * 4);
    }

    @Override
    public void initChannel(Channel ch) {
        // Get pipeline-instance
        ChannelPipeline pipeline = ch.pipeline();

        //Add logging-handler if enabled
        if (endpoint.builder().isLogging())
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));

        if (endpoint.builder().getCompression() != null && endpoint.builder().getCompression() != Compression.NONE) {
            pipeline.addLast(endpoint.builder().getCompression().getEncoder());
            pipeline.addLast(endpoint.builder().getCompression().getDecoder());
        }

        if (endpoint.isClient()) {
            pipeline.addLast("idle-state", new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
            pipeline.addLast("idle-handler", new EndpointIdleHandler(endpoint));
        }

        // Add the ByteToMessageCodec<Object>
        if (endpoint.builder().getProtocol() != null && endpoint.builder().getProtocol() == Protocol.NATIVE)
            pipeline.addLast("packet-codec", new NativeCodec(endpoint));
        else if(endpoint.builder().getProtocol() != null && endpoint.builder().getProtocol() == Protocol.SERIALIZABLE)
            pipeline.addLast("packet-codec", new SerializableCodec(endpoint));

        // Add the business-logic handler with async executor.
        pipeline.addLast(executorGroup, "netty-handler", new EndpointHandler(endpoint, channels));
    }
}
