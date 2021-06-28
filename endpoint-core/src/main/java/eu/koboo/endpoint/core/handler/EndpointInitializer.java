package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.AbstractEndpoint;
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
import io.netty.util.concurrent.EventExecutorGroup;

public class EndpointInitializer extends ChannelInitializer<Channel> {

    private final AbstractEndpoint endpoint;
    private final ChannelGroup channels;
    private final EventExecutorGroup executorGroup;

    public EndpointInitializer(AbstractEndpoint endpoint, ChannelGroup channels, EventExecutorGroup executorGroup) {
        this.endpoint = endpoint;
        this.channels = channels;
        this.executorGroup = executorGroup;
    }

    @Override
    public void initChannel(Channel ch) {
        try {
            // Get pipeline-instance
            ChannelPipeline pipeline = ch.pipeline();

            if (endpoint.builder().isFraming()) {
                pipeline.addLast("length-decoder", new LengthFieldBasedFrameDecoder(2048, 0, 4, 0, 4));
                pipeline.addLast("length-encoder", new LengthFieldPrepender(4));
            }

            //Add logging-handler if enabled
            if (endpoint.builder().isLogging())
                pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));

            if (endpoint.builder().getCompression() != null && endpoint.builder().getCompression() != Compression.NONE) {
                pipeline.addLast(endpoint.builder().getCompression().getEncoder());
                pipeline.addLast(endpoint.builder().getCompression().getDecoder());
            }

            if(endpoint.builder().isUsingTimeouts()) {
                pipeline.addLast("idle-state", new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
                pipeline.addLast("idle-handler", new EndpointIdleHandler(endpoint));
            }

            pipeline.addLast("endpoint-codec", new EndpointCodec(endpoint));

            if (endpoint.builder().isProcessing() && executorGroup != null) {
                pipeline.addLast(executorGroup, "endpoint-handler", new EndpointHandler(endpoint, channels));
            } else {
                pipeline.addLast("endpoint-handler", new EndpointHandler(endpoint, channels));
            }
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }
}
