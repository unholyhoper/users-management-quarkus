package request;

public class UpdateProfileRequest {

    String user;
    String username;
    String email;

    public UpdateProfileRequest(String user, String username, String email) {
        this.user = user;
        this.username = username;
        this.email = email;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
