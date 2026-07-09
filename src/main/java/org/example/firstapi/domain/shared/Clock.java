package org.example.firstapi.domain.shared;

import java.time.OffsetDateTime;

public interface Clock {

    OffsetDateTime now();
}
