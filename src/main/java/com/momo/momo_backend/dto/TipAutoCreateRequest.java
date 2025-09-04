package com.momo.momo_backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipAutoCreateRequest {
    private String url;
    private Long storageNo; // 꿀팁을 저장할 보관함 ID
}
