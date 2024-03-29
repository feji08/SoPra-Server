package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) throws ParseException {
    newUser.setToken(UUID.randomUUID().toString());
      /**
       * make registered user online
       */
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate(new Date());
    if(checkIfUsernameExist(newUser)){
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is taken!");//409
    }
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public void update(Long userId, UserPostDTO userPostDTO){
      User userOld = userRepository.findById(userId).get();
      User userByUsername = userRepository.findByUsername(userPostDTO.getUsername());

      String baseErrorMessage = "The %s provided %s already taken!";
      if (userByUsername != null && userOld != userByUsername) {
          throw new ResponseStatusException(HttpStatus.CONFLICT,
                  String.format(baseErrorMessage, "username", "is"));
      }

      if (userPostDTO.getUsername()!=null) {
          userOld.setUsername(userPostDTO.getUsername());
      }
      if (userPostDTO.getBirthday()!=null) {
          userOld.setBirthday(userPostDTO.getBirthday());
      }
      userRepository.save(userOld);
      userRepository.flush();
  }

  public User getUserByUsername(String username) { return userRepository.findByUsername(username); }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User getUserByToken(String token) {return userRepository.findByToken(token);}

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          String.format(baseErrorMessage, "username", "is"));
    }
  }

    /**
     * add login check
     */
  public boolean checkIfUsernameExist(User user){
      User userByUsername = userRepository.findByUsername(user.getUsername());
      return userByUsername != null;
  }
  public boolean checkIfPasswordMatch(User user){
      User userByUsername = userRepository.findByUsername(user.getUsername());
      return userByUsername.getPassword().equals(user.getPassword());
  }

    public void login(User foundUser){
        foundUser.setStatus(UserStatus.ONLINE);
    }
    public void logout(User foundUser){
        foundUser.setStatus(UserStatus.OFFLINE);
    }
}
