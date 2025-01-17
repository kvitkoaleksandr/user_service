package school.faang.user_service.dto;

import lombok.Data;
import school.faang.user_service.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecommendationRequestDto {
    private long id;
    private String message;
    private RequestStatus status;
    private List<Long> skillsIds;
    private long requesterId;
    private long receiverId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
