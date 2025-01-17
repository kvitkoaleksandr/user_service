package school.faang.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import school.faang.user_service.dto.filter.SubscriptionFilterDto;
import school.faang.user_service.dto.subscription.SubscriptionDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.SubscriptionMapper;
import school.faang.user_service.repository.SubscriptionRepository;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private long followeeId;
    private long followerId;
    private int expectedCount;
    private SubscriptionFilterDto filter;
    private User user;
    private SubscriptionDto subscriptionDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        followerId = 2L;
        followeeId = 1L;

        expectedCount = 5;

        user = new User();
        user.setId(1L);
        user.setUsername("John Doe");
        user.setEmail("john@example.com");

        subscriptionDto = new SubscriptionDto();
        subscriptionDto.setId(1L);
        subscriptionDto.setUsername("John Doe");
        subscriptionDto.setEmail("john@example.com");

        filter = new SubscriptionFilterDto();
        filter.setNamePattern("John");
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

    @Test
    void testGetFollowersShouldReturnFilteredSubscribers() {
        when(subscriptionRepository.findByFolloweeId(followeeId)).thenReturn(Stream.of(user));
        when(subscriptionMapper.toDto(user)).thenReturn(subscriptionDto);

        List<SubscriptionDto> result = subscriptionService.getFollowers(followeeId, filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subscriptionDto, result.get(0));

        verify(subscriptionRepository, times(1)).findByFolloweeId(followeeId);
        verify(subscriptionMapper, times(1)).toDto(user);
    }

    @Test
    void testGetFollowersWhenNoSubscribersShouldThrowException() {
        when(subscriptionRepository.findByFolloweeId(followeeId)).thenReturn(Stream.empty());

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            subscriptionService.getFollowers(followeeId, filter);
        });

        assertEquals("Подписчики не найдены", exception.getMessage());

        verify(subscriptionRepository, times(1)).findByFolloweeId(followeeId);
        verifyNoInteractions(subscriptionMapper);
    }

    @Test
    void testGetFollowersCountShouldReturnCount() {
        when(subscriptionRepository.findFollowersAmountByFolloweeId(followeeId)).thenReturn(expectedCount);

        int actualCount = subscriptionService.getFollowersCount(followeeId);

        assertEquals(expectedCount, actualCount);
        verify(subscriptionRepository, times(1)).findFollowersAmountByFolloweeId(followeeId);
    }

    @Test
    void testGetFollowersCountWhenNoFollowersShouldThrowException() {
        when(subscriptionRepository.findFollowersAmountByFolloweeId(followeeId)).thenReturn(0);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            subscriptionService.getFollowersCount(followeeId);
        });

        assertEquals("Подписчики не найдены", exception.getMessage());
        verify(subscriptionRepository, times(1)).findFollowersAmountByFolloweeId(followeeId);
    }

    @Test
    void testGetFollowingShouldReturnFilteredSubscriptions() {
        when(subscriptionRepository.findByFollowerId(followerId)).thenReturn(Stream.of(user));
        when(subscriptionMapper.toDto(user)).thenReturn(subscriptionDto);

        List<SubscriptionDto> result = subscriptionService.getFollowing(followerId, filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subscriptionDto, result.get(0));

        verify(subscriptionRepository, times(1)).findByFollowerId(followerId);
        verify(subscriptionMapper, times(1)).toDto(user);
    }

    @Test
    void testGetFollowingWhenNoSubscriptionsShouldThrowException() {
        when(subscriptionRepository.findByFollowerId(followerId)).thenReturn(Stream.empty());

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            subscriptionService.getFollowing(followerId, filter);
        });

        assertEquals("Подписки не найдены", exception.getMessage());

        verify(subscriptionRepository, times(1)).findByFollowerId(followerId);
        verifyNoInteractions(subscriptionMapper);
    }

    @Test
    void testGetFollowingWithFilterShouldReturnMatchingSubscriptions() {
        filter.setCityPattern("exampleCity");
        user.setCity("exampleCity");

        when(subscriptionRepository.findByFollowerId(followerId)).thenReturn(Stream.of(user));
        when(subscriptionMapper.toDto(user)).thenReturn(subscriptionDto);

        List<SubscriptionDto> result = subscriptionService.getFollowing(followerId, filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subscriptionDto, result.get(0));

        verify(subscriptionRepository, times(1)).findByFollowerId(followerId);
        verify(subscriptionMapper, times(1)).toDto(user);
    }

    @Test
    void testGetFollowingCountShouldReturnCount() {
        when(subscriptionRepository.findFolloweesAmountByFollowerId(followerId)).thenReturn(expectedCount);

        int actualCount = subscriptionService.getFollowingCount(followerId);

        assertEquals(expectedCount, actualCount);
        verify(subscriptionRepository, times(1)).findFolloweesAmountByFollowerId(followerId);
    }

    @Test
    void testGetFollowingCountWhenNoSubscriptionsShouldThrowException() {
        when(subscriptionRepository.findFolloweesAmountByFollowerId(followerId)).thenReturn(0);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            subscriptionService.getFollowingCount(followerId);
        });

        assertEquals("Подписки не найдены", exception.getMessage()); // Сообщение обновлено
        verify(subscriptionRepository, times(1)).findFolloweesAmountByFollowerId(followerId);
    }
}