package school.faang.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.SubscriptionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private SubscriptionService subscriptionService;
    private long followerId;
    private long followeeId;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        subscriptionService = new SubscriptionService(subscriptionRepository);
        followerId = 1L;
        followeeId = 2L;
    }

    @Test
    void testFollowUserWhenSubscriptionDoesNotExistShouldCallRepository() {
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(false);

        subscriptionService.followUser(followerId, followeeId);

        verify(subscriptionRepository, times(1)).followUser(followerId, followeeId);
    }

    @Test
    void testFollowUserWhenSubscriptionExistsShouldThrowException() {
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(true);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> subscriptionService.followUser(followerId, followeeId)
        );

        assertEquals(
                "Пользователь с ID " + followerId + " уже подписан на пользователя с ID " + followeeId,
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).followUser(anyLong(), anyLong());
    }

    @Test
    void testUnfollowUserWhenSubscriptionExistsShouldCallRepository() {
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(true);

        subscriptionRepository.unfollowUser(followerId, followeeId);

        verify(subscriptionRepository, times(1)).unfollowUser(followerId, followeeId);
    }

    @Test
    void testUnfollowUserWhenSubscriptionDoesNotExistShouldThrowException() {
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(false);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> subscriptionService.unfollowUser(followerId, followeeId)
        );

        assertEquals(
                "Подписка не найдена. Нельзя отписаться от пользователя с ID " + followeeId +
                        ", на которого вы не подписаны.",
                exception.getMessage()
        );

        verify(subscriptionRepository, never()).unfollowUser(anyLong(), anyLong());
    }
}