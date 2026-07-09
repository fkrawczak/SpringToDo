package org.example.firstapi.infrastructure.core;

import org.example.firstapi.domain.shared.Clock;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class SystemClock implements Clock {

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
