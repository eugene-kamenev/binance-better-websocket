package com.binance;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.exception.BinanceApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class BinanceWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectReader objectReader;
    private final BinanceApiCallback<T> callback;

    WebSocket current = null;

    BinanceWebSocketListener(BinanceApiCallback<T> callback, Class <T> eventClass) {
        this.callback = callback;
        this.objectReader = MAPPER.readerFor(eventClass);
    }

    @Override
    public void onPingFrame(byte[] payload) {
        log.info("WebSocket " + current.toString() + " received ping, sending pong back..");
        this.current.sendPongFrame(payload);
    }

    /**
     * Remember that callback should never block event loop!!
     */
    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            T event = objectReader.readValue(payload);
            this.callback.onResponse(event);
        } catch (IOException ex) {
            log.error("Error at WebSocket " + current.toString(), ex);
            throw new BinanceApiException(ex);
        }
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.current = websocket;
        log.info("WebSocket " + current.toString() + " opened");
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        log.info("WebSocket " + current.toString() + " was closed..");
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error at WebSocket " + current.toString(), t);
    }
}


