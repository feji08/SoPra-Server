package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class UserPostDTO {

    private Long id;

  private String password;

  private String birthday;

  private String token;
  private String username;

  public String getPassword() {
      return password;
  }

  public void setPassword(String password) {
      this.password = password;
  }

    public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
