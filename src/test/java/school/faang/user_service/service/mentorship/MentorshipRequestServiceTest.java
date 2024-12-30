package school.faang.user_service.service.mentorship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.entity.MentorshipRequest;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.MentorshipRequestMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.mentorship.MentorshipRequestRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorshipRequestServiceTest {

    @Mock
    private MentorshipRequestRepository mentorshipRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MentorshipRequestMapper mentorshipRequestMapper;

    @InjectMocks
    private MentorshipRequestService mentorshipRequestService;

    private User requester;
    private User receiver;
    private MentorshipRequest mentorshipRequest;
    private MentorshipRequestDto mentorshipRequestDto;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);

        receiver = new User();
        receiver.setId(2L);

        mentorshipRequestDto = new MentorshipRequestDto();
        mentorshipRequestDto.setMenteeId(1L);
        mentorshipRequestDto.setMentorId(2L);
        mentorshipRequestDto.setDescription("Need guidance on Java.");

        mentorshipRequest = new MentorshipRequest();
        mentorshipRequest.setRequester(requester);
        mentorshipRequest.setReceiver(receiver);
        mentorshipRequest.setDescription("Need guidance on Java.");
        mentorshipRequest.setStatus(RequestStatus.PENDING);
        mentorshipRequest.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testRequestMentorshipSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(mentorshipRequestRepository.findLatestRequest(1L, 2L))
                .thenReturn(Optional.empty());
        when(mentorshipRequestMapper.toEntity(mentorshipRequestDto)).thenReturn(mentorshipRequest);
        when(mentorshipRequestRepository.save(any())).thenReturn(mentorshipRequest);
        when(mentorshipRequestMapper.toDto(any())).thenReturn(mentorshipRequestDto);

        MentorshipRequestDto result = mentorshipRequestService.requestMentorship(mentorshipRequestDto);

        assertNotNull(result);
        assertEquals(mentorshipRequestDto.getDescription(), result.getDescription());
        verify(mentorshipRequestRepository, times(1)).save(any());
    }

    @Test
    void testRequestMentorshipRequesterNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                mentorshipRequestService.requestMentorship(mentorshipRequestDto));

        assertEquals("Пользователь-запрашивающий не найден.", exception.getMessage());
    }

    @Test
    void testRequestMentorshipReceiverNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                mentorshipRequestService.requestMentorship(mentorshipRequestDto));

        assertEquals("Пользователь-ментор не найден.", exception.getMessage());
    }

    @Test
    void testRequestMentorshipSelfRequest() {
        mentorshipRequestDto.setMenteeId(1L);
        mentorshipRequestDto.setMentorId(1L);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                mentorshipRequestService.requestMentorship(mentorshipRequestDto));

        assertEquals("Нельзя отправить запрос на менторство самому себе.", exception.getMessage());
    }

    @Test
    void testRequestMentorshipTooFrequentRequest() {
        mentorshipRequest.setCreatedAt(LocalDateTime.now().minusMonths(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(mentorshipRequestRepository.findLatestRequest(1L, 2L))
                .thenReturn(Optional.of(mentorshipRequest));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                mentorshipRequestService.requestMentorship(mentorshipRequestDto));

        assertEquals("Запрос на менторство можно отправлять раз в 3 месяца.", exception.getMessage());
    }

    @Test
    void testAcceptMentorshipRequestSuccess() {
        mentorshipRequest.setStatus(RequestStatus.PENDING);
        when(mentorshipRequestRepository.findById(1L)).thenReturn(Optional.of(mentorshipRequest));

        mentorshipRequestService.acceptMentorshipRequest(1L);

        assertEquals(RequestStatus.ACCEPTED, mentorshipRequest.getStatus());
        verify(mentorshipRequestRepository, times(1)).save(mentorshipRequest);
    }

    @Test
    void testAcceptMentorshipRequestAlreadyProcessed() {
        mentorshipRequest.setStatus(RequestStatus.ACCEPTED);
        when(mentorshipRequestRepository.findById(1L)).thenReturn(Optional.of(mentorshipRequest));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> mentorshipRequestService.acceptMentorshipRequest(1L));

        assertEquals("Запрос уже обработан.", exception.getMessage());
        verify(mentorshipRequestRepository, never()).save(any(MentorshipRequest.class));
    }
}