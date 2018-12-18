package com.binance;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;

import java.time.Duration;

abstract class HttpUtils {

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(10);

    /**
     * @param eventLoop
     * @return new instance of AsyncHttpClient for EventLoop
     */
    public static AsyncHttpClient newAsyncHttpClient(EventLoopGroup eventLoop, int maxFrameSize, int maxBufferSize) {
        DefaultAsyncHttpClientConfig.Builder config = Dsl.config()
                .setEventLoopGroup(eventLoop)
                .addChannelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(DEFAULT_CONNECTION_TIMEOUT.toMillis()))
                .setWebSocketMaxFrameSize(maxFrameSize)
                .setWebSocketMaxBufferSize(maxBufferSize);
        return Dsl.asyncHttpClient(config);
    }
}
