package request;

public class UpdatePasswordRequest {


    String password;

    String repeatedPassword;

    public UpdatePasswordRequest() {
    }

    public UpdatePasswordRequest(String password, String repeatedPassword) {
        this.password = password;
        this.repeatedPassword = repeatedPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatedPassword() {
        return repeatedPassword;
    }

    public void setRepeatedPassword(String repeatedPassword) {
        this.repeatedPassword = repeatedPassword;
    }
}
