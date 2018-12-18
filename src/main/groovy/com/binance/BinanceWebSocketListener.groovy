package com.binance

import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.exception.BinanceApiException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import groovy.transform.CompileStatic
import org.asynchttpclient.ws.WebSocket
import org.asynchttpclient.ws.WebSocketListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class BinanceWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketListener.class)
    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final ObjectReader objectReader
    private final BinanceApiCallback<T> callback

    WebSocket current = null

    BinanceWebSocketListener(BinanceApiCallback<T> callback, Class <T> eventClass) {
        this.callback = callback
        this.objectReader = MAPPER.readerFor(eventClass)
    }

    @Override
    void onPingFrame(byte[] payload) {
        log.info("WebSocket $current received ping, sending pong back..")
        this.current.sendPongFrame(payload)
    }

    /**
     * Remember that callback should never block event loop!
     *
     * @param payload
     * @param finalFragment
     * @param rsv
     */
    @Override
    void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            T event = objectReader.readValue(payload)
            this.callback.onResponse(event)
        } catch (IOException ex) {
            throw new BinanceApiException(ex)
        }
    }

    @Override
    void onOpen(WebSocket websocket) {
        this.current = websocket
        log.info("WebSocket $current opened")
    }

    @Override
    void onClose(WebSocket websocket, int code, String reason) {
        log.info("WebSocket $current was closed..")
    }

    @Override
    void onError(Throwable t) {
        log.error("Error for WebSocket $current", t)
    }
}


