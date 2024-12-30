package school.faang.user_service.dto;

import lombok.Data;
import school.faang.user_service.entity.RequestStatus;

import java.time.LocalDateTime;

@Data
public class MentorshipRequestDto {
    private Long id;
    private Long menteeId;
    private Long mentorId;
    private String description;
    private LocalDateTime createdAt;
    private RequestStatus status;
}