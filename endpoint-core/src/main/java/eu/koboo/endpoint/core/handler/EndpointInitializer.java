package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class EndpointInitializer extends ChannelInitializer<Channel> {

    private final Endpoint endpoint;
    private final ChannelGroup channels;
    private final EventExecutorGroup executorGroup;
    private Constructor<? extends AbstractEndpointCodec<?>> handlerConstructor;

    public EndpointInitializer(Endpoint endpoint, ChannelGroup channels) {
        this.endpoint = endpoint;
        this.channels = channels;
        int cores = Runtime.getRuntime().availableProcessors();
        this.executorGroup = new DefaultEventExecutorGroup(cores * 4);

        try {
             handlerConstructor = endpoint.builder().getEndpointCodecClass().getConstructor(Endpoint.class);
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
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

        try {
            AbstractEndpointCodec<?> endpointCodec = endpoint.builder().getEndpointCodecClass().cast(handlerConstructor.newInstance(endpoint));
            pipeline.addLast("packet-codec", endpointCodec);

            pipeline.addLast(executorGroup, "netty-handler", new EndpointHandler(endpoint, channels));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            endpoint.onException(getClass(), e);
        }
    }
}
