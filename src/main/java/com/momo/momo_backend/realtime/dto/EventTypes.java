package com.momo.momo_backend.realtime.dto;

/** 실시간 이벤트 타입 (v1 계약) */
public final class EventTypes {
    private EventTypes() {}

    public static final String TIP_NEW = "tip:new";
    public static final String NOTIFICATION_NEW = "notification:new";
    public static final String TIP_UPDATE = "tip:update";
}
