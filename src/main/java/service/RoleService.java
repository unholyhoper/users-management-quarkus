package service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class RoleService {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    public RoleRepresentation getUserRole() {
        return getRoleByRoleName("user");
    }

    public RoleRepresentation getAdminRole() {
        return getRoleByRoleName("admin");
    }

    public String getRole(Collection<RoleRepresentation> roleRepresentations){
        String role = "guest";
        if (roleRepresentations.contains(getAdminRole())){
            return "admin";
        }

        if (roleRepresentations.contains(getUserRole())){
            return "user";
        }

        return role;
    }

    private RoleRepresentation getRoleByRoleName(String roleName) {
        return keycloak.realm(realm).roles().get(roleName).toRepresentation();
    }

}
