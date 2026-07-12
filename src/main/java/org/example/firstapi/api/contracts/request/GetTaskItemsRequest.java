package org.example.firstapi.api.contracts.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.firstapi.domain.model.taskitem.TaskItemStatus;

import java.util.ArrayList;
import java.util.List;

public class GetTaskItemsRequest {

    @Parameter(description = "Statuses to include", example = "NEW,IN_PROGRESS")
    private List<TaskItemStatus> statuses = new ArrayList<>();

    @Parameter(description = "Page number (counted from 1)", example = "1")
    @Min(1)
    private int page = 1;

    @Parameter(description = "Number of items per page", example = "10")
    @Min(1)
    @Max(100)
    private int size = 10;

    @Parameter(description = "Case-insensitive text searched in title", example = "test")
    private String search;

    public List<TaskItemStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<TaskItemStatus> statuses) {
        this.statuses = statuses == null ? new ArrayList<>() : statuses;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
