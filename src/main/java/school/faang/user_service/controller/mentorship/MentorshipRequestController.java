package school.faang.user_service.controller.mentorship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.service.mentorship.MentorshipRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentorship/request")
public class MentorshipRequestController {
    private final MentorshipRequestService mentorshipRequestService;

    @PostMapping //Отправить запрос на менторство
    public MentorshipRequestDto requestMentorship(@RequestBody MentorshipRequestDto mentorshipRequest) {
        validateRequest(mentorshipRequest);
        return mentorshipRequestService.requestMentorship(mentorshipRequest);
    }

    @PostMapping("/{id}/accept")
    public void acceptRequest(@PathVariable long id) {
        mentorshipRequestService.acceptMentorshipRequest(id);
    }

    @PostMapping("/{id}/reject")
    public void rejectRequest(@PathVariable long id, @RequestParam String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Причина отклонения не может быть пустой.");
        }
        mentorshipRequestService.rejectMentorshipRequest(id, reason);
    }

    private void validateRequest(MentorshipRequestDto mentorshipRequest) {
        if (mentorshipRequest.getDescription() == null || mentorshipRequest.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Описание запроса на менторство не может быть пустым.");
        }
    }
}