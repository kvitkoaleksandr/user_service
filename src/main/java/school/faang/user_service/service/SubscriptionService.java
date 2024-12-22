package school.faang.user_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.filter.SubscriptionFilterDto;
import school.faang.user_service.dto.subscription.SubscriptionDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.SubscriptionMapper;
import school.faang.user_service.repository.SubscriptionRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public void followUser(long followerId, long followeeId) {
        if (subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            log.error("Попытка подписки: пользователь с ID {} уже подписан на пользователя с ID {}",
                    followerId, followeeId);
            throw new DataValidationException(
                    "Пользователь с ID " + followerId + " уже подписан на пользователя с ID " + followeeId
            );
        }

        subscriptionRepository.followUser(followerId, followeeId);
        log.info("Пользователь с ID {} успешно подписался на пользователя с ID {}", followerId, followeeId);
    }

    @Transactional
    public void unfollowUser(long followerId, long followeeId) {
        if (!subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            log.warn("Попытка отписаться от несуществующей подписки: followerId={}, followeeId={}",
                    followerId, followeeId);
            throw new DataValidationException(
                    "Подписка не найдена. Нельзя отписаться от пользователя с ID " + followeeId +
                            ", на которого вы не подписаны."
            );
        }

        subscriptionRepository.unfollowUser(followerId, followeeId);
        log.info("Пользователь с ID {} отписался от пользователя с ID {}", followerId, followeeId);
    }

    @Transactional
    public List<SubscriptionDto> getFollowers(long followeeId, SubscriptionFilterDto filter) {
        try (Stream<User> userStream = subscriptionRepository.findByFolloweeId(followeeId)) {

            List<User> userList = userStream.toList();

            if (userList.isEmpty()) {
                log.warn("Попытка получить несуществующих подписчиков для followeeId={}", followeeId);
                throw new DataValidationException("Подписчики не найдены");
            }

            return filterUsers(userList, filter);
        }
    }

    private List<SubscriptionDto> filterUsers(List<User> userList, SubscriptionFilterDto filter) {
        return userList.stream()
                .filter(user -> filter.getNamePattern() == null || user.getUsername().contains(filter.getNamePattern()))
                .filter(user -> filter.getEmailPattern() == null || user.getEmail().contains(filter.getEmailPattern()))
                .filter(user -> filter.getCityPattern() == null || user.getCity().contains(filter.getCityPattern()))
                .filter(user -> filter.getPhonePattern() == null || user.getPhone().contains(filter.getPhonePattern()))
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional
    public int getFollowersCount(long followeeId) {
        int count = subscriptionRepository.findFollowersAmountByFolloweeId(followeeId);
        if (count == 0) {
            log.warn("У пользователя с ID {}, нет подписчиков", followeeId);
            throw new DataValidationException("Подписчики не найдены");
        }

        return count;
    }

    @Transactional
    public List<SubscriptionDto> getFollowing(long followerId, SubscriptionFilterDto filter) {
        try (Stream<User> userStream = subscriptionRepository.findByFollowerId(followerId)) {
            List<User> userList = userStream.toList();

            if (userList.isEmpty()) {
                log.warn("У пользователя с ID {} нет подписок", followerId);
                throw new DataValidationException("Подписки не найдены");
            }

            return filterUsers(userList, filter);
        }
    }

    @Transactional
    public int getFollowingCount(long followerId) {
        int count = subscriptionRepository.findFolloweesAmountByFollowerId(followerId);
        if (count == 0) {
            log.warn("У пользователя с ID {}, нет подписок", followerId);
            throw new DataValidationException("Подписки не найдены");
        }

        return count;
    }
}