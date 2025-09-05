package com.momo.momo_backend.realtime.debug;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class StompSessionLogger {

    @EventListener
    public void onConnect(SessionConnectEvent e) {
        var p = e.getUser();
        log.info("[WS] CONNECT sessionId={}, principal={}",
                e.getMessage().getHeaders().get("simpSessionId"),
                p != null ? p.getName() : null);
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent e) {
        var p = e.getUser();
        var dest = e.getMessage().getHeaders().get("simpDestination");
        log.info("[WS] SUBSCRIBE sessionId={}, principal={}, dest={}",
                e.getMessage().getHeaders().get("simpSessionId"),
                p != null ? p.getName() : null,
                dest);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        var p = e.getUser();
        log.info("[WS] DISCONNECT sessionId={}, principal={}",
                e.getSessionId(),
                p != null ? p.getName() : null);
    }
}
