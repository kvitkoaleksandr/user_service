package school.faang.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.service.SubscriptionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    //followerId - id пользователя, который хочет подписаться
    //followeeId - id того, на кого хотят подписаться.
    @PostMapping("/follow")
    public void followUser(@RequestParam long followerId, @RequestParam long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Нельзя подписываться на самого себя!");
        }
        subscriptionService.followUser(followerId, followeeId);
    }

    @DeleteMapping("/unfollow")
    public void unfollowUser(@RequestParam long followerId, @RequestParam long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Нельзя отписаться от самого себя!");
        }
        subscriptionService.unfollowUser(followerId, followeeId);
    }

}