package com.tuckersoft.branchengine.dto;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
public final class DecisionDtos {
    private DecisionDtos(){}
    public record CreateRequest(@NotNull Long playthroughId,@NotBlank @Size(min=10) String rawInput,@NotBlank String impactLevel){}
    public record Response(Long id,Long playthroughId,String playerTag,String sourceNodeCode,String resolvedNodeCode,
                           String rawInput,String branchType,String impactLevel,String handlerUnit,String outcomeCode,
                           String status,String playthroughStatus,Integer lucidity,Integer controlLevel,String endingCode,
                           Instant createdAt,Instant updatedAt){}
    public record RealityLogResponse(Long id,Long decisionId,String recipientEmail,String subject,String logStatus,
                                     String errorMessage,Instant sentAt,Instant createdAt){}
    public record PageResponse<T>(List<T> content,long totalElements,int totalPages,int currentPage,int size){}
}
