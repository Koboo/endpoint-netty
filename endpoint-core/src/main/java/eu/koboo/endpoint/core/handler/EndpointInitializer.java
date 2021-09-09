package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.EndpointCore;
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

    public static final String LENGTH_DECODER = "length-decoder";
    public static final String LENGTH_ENCODER = "length-encoder";

    public static final String LOGGING_HANDLER = "logging-handler";

    public static final String COMPRESSION_DECODER = "compression-decoder";
    public static final String COMPRESSION_ENCODER = "compression-encoder";

    public static final String IDLE_STATE_THROWER = "idle-state-thrower";
    public static final String IDLE_STATE_HANDLER = "idle-state-handler";

    public static final String CODEC_HANDLER = "codec-handler";

    public static final String ENDPOINT_HANDLER = "endpoint-handler";

    private static final LoggingHandler LOGGING_HANDLER_INSTANCE = new LoggingHandler(LogLevel.TRACE);
    private static final LengthFieldPrepender LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(4);

    private final AbstractEndpoint endpoint;
    private final EventExecutorGroup processExecutor = new DefaultEventExecutorGroup(EndpointCore.CORES * 2);
    private final ChannelGroup channels;

    public EndpointInitializer(AbstractEndpoint endpoint, ChannelGroup channels) {
        this.endpoint = endpoint;
        this.channels = channels;
    }

    @Override
    public void initChannel(Channel ch) {
        try {
            // Get pipeline-instance
            ChannelPipeline pipeline = ch.pipeline();

            if (endpoint.builder().isFraming()) {
                pipeline.addLast(LENGTH_DECODER, new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast(LENGTH_ENCODER, LENGTH_FIELD_PREPENDER);
            }

            //Add logging-handler if enabled
            if (endpoint.builder().isLogging())
                pipeline.addLast(LOGGING_HANDLER, LOGGING_HANDLER_INSTANCE);

            if (endpoint.builder().getCompression() != null && endpoint.builder().getCompression() != Compression.NONE) {
                pipeline.addLast(COMPRESSION_DECODER, endpoint.builder().getCompression().getDecoder());
                pipeline.addLast(COMPRESSION_ENCODER, endpoint.builder().getCompression().getEncoder());
            }

            if(endpoint.builder().isUsingTimeouts()) {
                pipeline.addLast(IDLE_STATE_THROWER, new IdleStateHandler(endpoint.builder().getReadTimeout(), endpoint.builder().getWriteTimeout(), 0));
                pipeline.addLast(IDLE_STATE_HANDLER, new EndpointIdleHandler(endpoint));
            }

            pipeline.addLast(CODEC_HANDLER, new EndpointCodec(endpoint));

            if (endpoint.builder().isProcessing() && (!processExecutor.isShutdown() && !processExecutor.isTerminated())) {
                pipeline.addLast(processExecutor, ENDPOINT_HANDLER, new EndpointHandler(endpoint, channels));
            } else {
                pipeline.addLast(ENDPOINT_HANDLER, new EndpointHandler(endpoint, channels));
            }
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }

    public void shutdown() {
        processExecutor.shutdownGracefully();
    }
}
