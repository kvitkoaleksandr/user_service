package school.faang.user_service.dto.filter;

import lombok.Data;
import school.faang.user_service.entity.RequestStatus;

@Data
public class MentorshipRequestFilterDto {
    private String description;
    private Long menteeId;
    private Long mentorId;
    private RequestStatus status;
}