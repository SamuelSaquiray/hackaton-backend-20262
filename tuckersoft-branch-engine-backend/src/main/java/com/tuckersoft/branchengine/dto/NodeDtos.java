package com.tuckersoft.branchengine.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class NodeDtos {

    private NodeDtos() {}

    public record CreateNodeRequest(
            @NotBlank
            @Size(min = 3, max = 40)
            String nodeCode,

            @NotBlank
            @Size(min = 3, max = 80)
            String title,

            @NotBlank
            @Size(min = 10)
            String sceneText,

            @NotNull
            @Min(1)
            Integer branchCapacity,

            String primaryBranchCode,
            String glitchBranchCode
    ) {}

    public record NodeResponse(
            Long id,
            String nodeCode,
            String title,
            String sceneText,
            Integer branchCapacity,
            Integer currentBranches,
            String primaryBranchCode,
            String glitchBranchCode,
            Instant createdAt
    ) {}

    public record PageResponse<T>(
            java.util.List<T> content,
            long totalElements,
            int totalPages,
            int currentPage,
            int size
    ) {}
}

