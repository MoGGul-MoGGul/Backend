package com.momo.momo_backend.realtime.security;

import com.momo.momo_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        // CONNECT 명령어에 대해서만 토큰 인증 수행
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateAndSetUser(accessor);
            // 헤더가 수정되었으므로 새 메시지를 만들어 반환
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        return message;
    }

    // CONNECT 명령어에서 토큰을 추출하고 인증을 수행하여 Principal을 설정합니다.
    private void authenticateAndSetUser(StompHeaderAccessor accessor) {
        String token = extractBearer(accessor);
        log.debug("STOMP CONNECT rawHeaderExists={}, tokenPrefix={}",
                accessor.getNativeHeader("Authorization") != null || accessor.getNativeHeader("authorization") != null,
                token != null && token.length() >= 10 ? token.substring(0, 10) : "null");

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String principalName = getPrincipalNameFromToken(token);

            if (StringUtils.hasText(principalName)) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        principalName, "N/A", Collections.emptyList()
                );
                accessor.setUser(auth); // STOMP 세션에 Principal 주입
                SecurityContextHolder.getContext().setAuthentication(auth); // SecurityContext에도 반영
                log.debug("STOMP CONNECT -> authenticated principal='{}'", principalName);
            }
        } else {
            log.debug("STOMP CONNECT without/invalid token (public endpoints may allow)");
        }
    }

    // 토큰에서 userNo를 추출하여 Principal 이름으로 사용합니다. userNo가 없으면 loginId로 폴백.
    private String getPrincipalNameFromToken(String token) {
        try {
            Long userNo = jwtTokenProvider.getUserNo(token);
            if (userNo != null) {
                log.debug("STOMP CONNECT parsed userNo={}", userNo);
                return String.valueOf(userNo); // 개인 큐 라우팅 키는 "userNo" 문자열
            }
        } catch (Exception e) {
            log.debug("STOMP CONNECT getUserNo threw: {}", e.toString());
        }

        // userNo 추출 실패 시 loginId로 폴백
        try {
            String loginId = jwtTokenProvider.getUserIdFromToken(token);
            if (StringUtils.hasText(loginId)) {
                log.warn("STOMP CONNECT -> token has no userNo; set principal to loginId='{}' "
                        + "(personal-queue routing expects userNo string)", loginId);
                return loginId;
            }
        } catch (Exception ignored) {
            // loginId 추출마저 실패하면, 인증 실패로 간주하고 null을 반환하는 것이 의도된 동작임.
        }

        return null;
    }

    // Authorization 헤더에서 Bearer 토큰을 추출합니다.
    private String extractBearer(StompHeaderAccessor accessor) {
        List<String> headers = accessor.getNativeHeader("Authorization");
        if (headers == null || headers.isEmpty()) {
            headers = accessor.getNativeHeader("authorization");
        }
        if (headers != null && !headers.isEmpty()) {
            String raw = headers.get(0);
            if (raw != null && raw.startsWith("Bearer ")) {
                return raw.substring(7);
            }
        }
        return null;
    }
}