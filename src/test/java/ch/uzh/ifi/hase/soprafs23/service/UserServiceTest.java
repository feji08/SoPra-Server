package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser1;
  private User testUser2;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser1 = new User();
    testUser1.setId(1L);
    testUser1.setUsername("testUsername");
    testUser1.setPassword("p");

    testUser2 = new User();
    testUser2.setId(1L);
    testUser2.setUsername("testUsername2");
    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.MARCH, 9);
    testUser2.setBirthday(cal.getTime());
    testUser2.setPassword("p");

    // when -> any object is being saved in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any()))
            .thenReturn(testUser1)
            .thenReturn(testUser2);
  }

  @Test
  public void createUser_validInputs_success() throws ParseException {
    // when -> any object is being saved in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser1);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser1.getId(), createdUser.getId());
    assertEquals(testUser1.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateName_throwsException() throws ParseException {
    // given -> a first user has already been created
    userService.createUser(testUser1);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser1);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser1));
  }

  @Test
  public void createUser_duplicateInputs_throwsException() throws ParseException {
    // given -> a first user has already been created
    userService.createUser(testUser1);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser1);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser1));
  }

    @Test
    public void updateUser_validUserAndUpdate_success() throws ParseException {
        // testUser
        User createdUser = userService.createUser(testUser1);
        UserPostDTO updatedUser = new UserPostDTO();
        updatedUser.setUsername("testUsername2");
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.MARCH, 9);
        updatedUser.setBirthday(cal.getTime());

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(createdUser));
        userService.update(1L,updatedUser);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(testUser2.getUsername(), createdUser.getUsername());
        String expected = formatter.format(testUser2.getBirthday());
        String actual = formatter.format(createdUser.getBirthday());
        assertEquals(expected,actual);
    }

}
