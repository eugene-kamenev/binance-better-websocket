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

public class BinanceWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectReader objectReader;
    private final BinanceApiCallback<T> callback;

    private WebSocket current = null;
    private String wsName = null;

    public BinanceWebSocketListener(BinanceApiCallback<T> callback, Class <T> eventClass) {
        this.callback = callback;
        this.objectReader = MAPPER.readerFor(eventClass);
    }

    @Override
    public void onPingFrame(byte[] payload) {
        log.info("WebSocket " + wsName + " received ping, sending pong back..");
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
            log.error("Error at WebSocket " + wsName, ex);
            throw new BinanceApiException(ex);
        }
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.current = websocket;
        this.wsName = websocket.toString();
        log.info("WebSocket " + wsName + " opened");
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        log.info("WebSocket " + wsName + " was closed..");
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error at WebSocket " + wsName, t);
    }

    public WebSocket getWebSocket() {
        return this.current;
    }
}


