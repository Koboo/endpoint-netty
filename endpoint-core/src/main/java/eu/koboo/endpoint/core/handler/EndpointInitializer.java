package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.EndpointCodec;
import eu.koboo.endpoint.core.util.Compression;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
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
        try {
            // Get pipeline-instance
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("length-decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8));
            pipeline.addLast("length-encoder", new LengthFieldPrepender(8));

            //Add logging-handler if enabled
            if (endpoint.builder().isLogging())
                pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));

            if (endpoint.builder().getCompression() != null && endpoint.builder().getCompression() != Compression.NONE) {
                pipeline.addLast(endpoint.builder().getCompression().getEncoder());
                pipeline.addLast(endpoint.builder().getCompression().getDecoder());
            }

            pipeline.addLast("idle-state", new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
            pipeline.addLast("idle-handler", new EndpointIdleHandler(endpoint));

            pipeline.addLast("native-codec", new EndpointCodec(endpoint));

            pipeline.addLast(executorGroup, "netty-handler", new EndpointHandler(endpoint, channels));
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }
}
