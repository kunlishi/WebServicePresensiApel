package com.polstat.WebServiceApel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PresensiBatchResponse {
    private long savedCount;
    private long ignoredCount;
    private Long scheduleId;
}
