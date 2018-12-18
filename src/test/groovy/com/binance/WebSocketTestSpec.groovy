package com.binance

import com.binance.api.client.domain.event.AllMarketTickersEvent
import com.binance.api.client.domain.event.DepthEvent
import com.binance.api.client.domain.market.AggTrade
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.ws.WebSocketUpgradeHandler
import spock.lang.Specification

class WebSocketTestSpec extends Specification {

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2)
    private final AsyncHttpClient client = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536, 65536)

    def 'test aggTrades'() {
        given:
        def events = []
        def socket = client
            .prepareGet("wss://stream.binance.com:9443/ws/btcusdt@aggTrade/ethusdt@aggTrade/manabtc@aggTrade/gobtc@aggTrade/gntbtc@aggTrade")
            .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                new BinanceWebSocketListener({ trade -> println trade; events << trade }, AggTrade)
        ).build()).get()
        when:
        Thread.sleep(10000L)

        then:
        events.size() > 0

        cleanup:
        socket.sendCloseFrame()
    }

    def 'test all tickers'() {
        given:
        def events = []
        def socket = client
                .prepareGet("wss://stream.binance.com:9443/ws/!ticker@arr")
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new BinanceWebSocketListener({ e -> println e; events << e }, AllMarketTickersEvent[])).build()).get()
        when:
        Thread.sleep(10000L)

        then:
        events.size() > 0

        cleanup:
        socket.sendCloseFrame()
    }

    def 'test depth'() {
        given:
        def events = []
        def socket = client
                .prepareGet("wss://stream.binance.com:9443/ws/btcusdt@aggTrade/ethusdt@depth/manabtc@depth/gobtc@depth/gntbtc@depth")
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new BinanceWebSocketListener({ e -> println e; events << e }, DepthEvent)).build()).get()
        when:
        Thread.sleep(10000L)

        then:
        events.size() > 0

        cleanup:
        socket.sendCloseFrame()
    }
}
