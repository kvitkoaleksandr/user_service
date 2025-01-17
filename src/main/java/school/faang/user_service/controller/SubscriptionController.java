package school.faang.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.dto.filter.SubscriptionFilterDto;
import school.faang.user_service.dto.subscription.SubscriptionDto;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.service.SubscriptionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    //followerId - id пользователя, который хочет подписаться
    //followeeId - id того, на кого хотят подписаться.
    @PostMapping("/follow") // Подписаться на пользователя
    public void followUser(@RequestParam long followerId, @RequestParam long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Нельзя подписываться на самого себя!");
        }

        subscriptionService.followUser(followerId, followeeId);
    }

    @DeleteMapping("/unfollow") // Отписаться от пользователя
    public void unfollowUser(@RequestParam long followerId, @RequestParam long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Нельзя отписаться от самого себя!");
        }

        subscriptionService.unfollowUser(followerId, followeeId);
    }

    @GetMapping("/getFollowers") // Получить всех подписчиков
    public List<SubscriptionDto> getFollowers(@RequestParam long followeeId, SubscriptionFilterDto filter) {
        if (followeeId <= 0) {
            throw new RuntimeException("Не корректное ID пользователя.");
        }

        return subscriptionService.getFollowers(followeeId, filter);
    }

    @GetMapping("/getFollowersCount") // Получить количество подписчиков
    public int getFollowersCount(@RequestParam long followeeId) {
        if (followeeId <= 0) {
            throw new RuntimeException("Не корректный ID пользователя.");
        }

        return subscriptionService.getFollowersCount(followeeId);
    }

    @GetMapping("/getFollowing") // Просмотреть свои подписки
    public List<SubscriptionDto> getFollowing(@RequestParam long followerId, SubscriptionFilterDto filter) {
        if (followerId <= 0) {
            throw new RuntimeException("Не корректное ID пользователя.");
        }

        return subscriptionService.getFollowing(followerId, filter);
    }

    @GetMapping("/getFollowingCount") // Получить количество своих подписок
    public int getFollowingCount(@RequestParam long followerId) {
        if (followerId <= 0) {
            throw new RuntimeException("Не корректный ID пользователя.");
        }

        return subscriptionService.getFollowingCount(followerId);
    }
}