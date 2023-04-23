package response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collection;

public class UserDTO {

    @JsonProperty("id")
    String id;
    String username;
    String email;
    String name;
    String role;

    public UserDTO() {
    }

    public UserDTO(UserRepresentation userRepresentation) {
        this.id = userRepresentation.getId();
        this.username = userRepresentation.getUsername();
        this.email = userRepresentation.getEmail();
        this.name = userRepresentation.getFirstName();
    }


    public UserDTO(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
