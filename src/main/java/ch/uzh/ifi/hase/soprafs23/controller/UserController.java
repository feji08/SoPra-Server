package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.LocalUserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public LocalUserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) throws ParseException {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToLocalUserGetDTO(createdUser);
  }

    @GetMapping("/users/{userId}")
    @ResponseBody
    public UserGetDTO getUser(@PathVariable Long userId) {
        // search for user
        if(userService.getUserById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!");
        }
        User foundUser = userService.getUserById(userId).get();
        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(foundUser);
    }

    @PutMapping("/users/{userId}")
    @ResponseBody
    public void updateUser(@PathVariable Long userId, @RequestBody UserPostDTO userPostDTO) {
        // search for user
        if(userService.getUserById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!");
        }
        userService.update(userId, userPostDTO);
    }

    /**
     * fetch localUser with token
     */
    @PostMapping("/users/localUser")
    @ResponseBody
    public UserGetDTO getLocalUser(@RequestBody UserPostDTO userPostDTO) {
        User foundUser = userService.getUserByToken(userPostDTO.getToken());
        // convert internal representation of user back to API
        if(foundUser==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!");
        }
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(foundUser);
    }


    /**
     * 1.check the validity of username
     * 2.check if password matches
     * 3.make user login
     */
    @PostMapping("/login")
    @ResponseBody
    public LocalUserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        //match username
        if(!userService.checkIfUsernameExist(userPostDTO)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username!");
        }
        //match password
        if(!userService.checkIfPasswordMatch(userPostDTO)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password!");
        }
        //return user
        User foundUser = userService.getUserByUsername(userPostDTO.getUsername());
        //login
        userService.login(foundUser);
        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToLocalUserGetDTO(foundUser);
    }


    /**
     * verify token and find user
     * make user logout
     */
    @PutMapping("/logout")
    @ResponseBody
    public void logout(@RequestBody UserPostDTO userPostDTO) {
        //match token
        User foundUser = userService.getUserByToken(userPostDTO.getToken());
        // convert internal representation of user back to API
        if(foundUser==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found!");
        }
        else if (foundUser.getStatus()== UserStatus.OFFLINE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"User already offline!");
        }
        userService.logout(foundUser);
    }
}
