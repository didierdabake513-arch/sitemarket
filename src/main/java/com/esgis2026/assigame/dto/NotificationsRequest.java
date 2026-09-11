package com.esgis2026.assigame.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationsRequest {
    private List<Map<String, Object>> preferences;
}
