package school.faang.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import school.faang.user_service.dto.subscription.SubscriptionDto;
import school.faang.user_service.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {
    SubscriptionDto toDto(User user);

    List<SubscriptionDto> toDto(List<User> users);
}