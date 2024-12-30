package school.faang.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.entity.MentorshipRequest;

@Mapper(componentModel = "spring")
public interface MentorshipRequestMapper {

    @Mapping(source = "menteeId", target = "requester.id")
    @Mapping(source = "mentorId", target = "receiver.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MentorshipRequest toEntity(MentorshipRequestDto dto);

    @Mapping(source = "requester.id", target = "menteeId")
    @Mapping(source = "receiver.id", target = "mentorId")
    MentorshipRequestDto toDto(MentorshipRequest entity);
}