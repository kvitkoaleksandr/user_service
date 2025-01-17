package school.faang.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.dto.filter.UserFilterDto;
import school.faang.user_service.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private static final long INVALID_ID_FOR_USER = -3L;
    private static final long VALID_USER_ID = 1L;
    private MockMvc mockMvc;
    @Mock
    private UserService service;
    @InjectMocks
    private UserController controller;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UserDto firstUser, secondUser;
    private List<Long> ids;

    @BeforeEach
    public void setUp() {
        //Arrange
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        firstUser = new UserDto();
        secondUser = new UserDto();
        firstUser.setId(1);
        secondUser.setId(2L);
        firstUser.setUsername("Sasha");
        secondUser.setUsername("Danilla");
        firstUser.setEmail("sasha@yandex.ru");
        secondUser.setEmail("Danilla@yandex.ru");
        ids = List.of(firstUser.getId(), secondUser.getId());
    }

    @Test
    public void testUserDtoIsNull() {
        //Assert
        assertThrows(
                RuntimeException.class,
                () -> controller.deactivatesUserProfile(INVALID_ID_FOR_USER)
        );
    }

    @Test
    public void testVerifyServiceDeactivatesUserProfile() throws Exception {
        //Arrange
        UserDto dto = new UserDto();
        dto.setId(VALID_USER_ID);
        dto.setActive(true);
        dto.setGoalsIds(List.of(VALID_USER_ID));
        dto.setOwnedEventsIds(List.of(VALID_USER_ID));
        //Act
        Mockito.when(service.deactivatesUserProfile(anyLong())).thenReturn(dto);
        //Assert
        mockMvc.perform(put("/api/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_USER_ID))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.goalsIds", hasSize(1)))
                .andExpect(jsonPath("$.ownedEventsIds", hasSize(1)));
    }

    @Test
    public void testGetUser() throws Exception {
        Mockito.when(service.getUser(anyLong())).thenReturn(firstUser);
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("Sasha")))
                .andExpect(jsonPath("$.email", is("sasha@yandex.ru")));
    }

    @Test
    public void testGetUsersByIds() throws Exception {
        List<UserDto> dtoList = Arrays.asList(firstUser, secondUser);
        Mockito.when(service.getUsersByIds(ids)).thenReturn(dtoList);
        mockMvc.perform(get("/api/users/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    public void testGetPremiumUsersWhenUserFilterDtoIsNotNull() {
        UserFilterDto userFilterDto = new UserFilterDto();
        List<UserDto> users = List.of(new UserDto());
        when(service.getPremiumUsers(userFilterDto)).thenReturn(users);

        List<UserDto> result = controller.getPremiumUsers(userFilterDto);
        assertThat(result).isNotNull();
    }

    @Test
    public void testGetEmptyListWhenNoSuchPremiumUsers() {
        UserFilterDto userFilterDto = new UserFilterDto();
        when(service.getPremiumUsers(userFilterDto)).thenReturn(Collections.emptyList());

        List<UserDto> result = controller.getPremiumUsers(userFilterDto);
        assertEquals(Collections.emptyList(), result);
    }
}