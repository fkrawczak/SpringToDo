package org.example.firstapi.application.dtos;

import java.util.List;

public record PageResult<T>(List<T> items, long total) {
}
