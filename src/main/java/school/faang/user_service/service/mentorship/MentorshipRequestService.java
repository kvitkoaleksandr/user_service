package school.faang.user_service.service.mentorship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.entity.MentorshipRequest;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.MentorshipRequestMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.mentorship.MentorshipRequestRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorshipRequestService {
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final UserRepository userRepository;
    private final MentorshipRequestMapper mentorshipRequestMapper;

    public MentorshipRequestDto requestMentorship(MentorshipRequestDto mentorshipRequestDto) {
        // Проверка, что запрос на менторство не отправляется самому себе
        if (mentorshipRequestDto.getMenteeId().equals(mentorshipRequestDto.getMentorId())) {
            throw new IllegalArgumentException("Нельзя отправить запрос на менторство самому себе.");
        }

        // Проверка, что ментор и менти существуют
        User requester = userRepository.findById(mentorshipRequestDto.getMenteeId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь-запрашивающий не найден."));
        User receiver = userRepository.findById(mentorshipRequestDto.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь-ментор не найден."));

        // Проверка, что запрос на менторство можно делать раз в 3 месяца
        Optional<MentorshipRequest> latestRequest = mentorshipRequestRepository.findLatestRequest(
                mentorshipRequestDto.getMenteeId(),
                mentorshipRequestDto.getMentorId()
        );
        if (latestRequest.isPresent() && latestRequest.get().getCreatedAt()
                .isAfter(LocalDateTime.now().minusMonths(3))) {
            throw new IllegalArgumentException("Запрос на менторство можно отправлять раз в 3 месяца.");
        }

        // Сохранение запроса
        MentorshipRequest mentorshipRequest = mentorshipRequestMapper.toEntity(mentorshipRequestDto);
        mentorshipRequest.setRequester(requester);
        mentorshipRequest.setReceiver(receiver);
        mentorshipRequest.setStatus(RequestStatus.PENDING);

        mentorshipRequest = mentorshipRequestRepository.save(mentorshipRequest);

        log.info("Запрос на менторство успешно создан: {}", mentorshipRequest);
        return mentorshipRequestMapper.toDto(mentorshipRequest);
    }

    public void acceptMentorshipRequest(long requestId) {
        MentorshipRequest request = mentorshipRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос на менторство не найден."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Запрос уже обработан.");
        }

        request.setStatus(RequestStatus.ACCEPTED);
        mentorshipRequestRepository.save(request);
    }

    public void rejectMentorshipRequest(long requestId, String rejectionReason) {
        MentorshipRequest request = mentorshipRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Запрос на менторство не найден."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Запрос уже обработан.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        mentorshipRequestRepository.save(request);
    }
}