package school.faang.user_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

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
}