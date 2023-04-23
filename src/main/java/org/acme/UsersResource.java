package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import request.UpdatePasswordRequest;
import request.UpdateProfileRequest;
import request.UserRequest;
import response.UserDTO;
import service.RoleService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api/users")
public class UsersResource {

    @Inject
    Keycloak keycloak;
    @Inject
    SecurityIdentity identity;


    @Inject
    RoleService roleService;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;
//    @GET
//    @Path("/me")
//    @NoCache
//    public User me() {
//        return new User(identity);
//    }

    @GET
    @Path("/me")
    @NoCache
    public Response me() {
        String username = identity.getPrincipal().getName();
        var user = keycloak.realm("SELECTION-ENGINE").users().search(username).get(0);
        UserDTO userDTO = new UserDTO(user);
        return Response.ok(userDTO, MediaType.APPLICATION_JSON).build();

    }
//    @PUT
//    @Path("/me")
//    @NoCache
//    public Response updatePassword(@QueryParam("password") String password) {
//        String username = identity.getPrincipal().getName();
//        var user = keycloak.realm("SELECTION-ENGINE").users().search(username).get(0);
//        verifyPassword(user.getId(),password);
//        UserDTO userDTO = new UserDTO(user);
//        return Response.ok(userDTO, MediaType.APPLICATION_JSON).build();
//
//    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response updateRole(@PathParam("id") String id, @QueryParam("role") String role) {
        assignRoles(id, Collections.singletonList(role));
        return Response.ok().build();
    }

    @PUT
    @Path("")
    public Response updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        String password = updatePasswordRequest.getPassword();
        String repeatedPassword = updatePasswordRequest.getRepeatedPassword();
        if (!password.equals(repeatedPassword)) {
            return Response.status(Response.Status.CONFLICT).entity("Passwords don't match").build();
        }
        String username = identity.getPrincipal().getName();
        var userRepresentation = keycloak.realm("SELECTION-ENGINE").users().search(username).get(0);
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);
        credentials.setTemporary(false);
        userRepresentation.setCredentials(Arrays.asList(credentials));

        UserResource userResource = keycloak.realm(realm).users().get(userRepresentation.getId());
        userResource.update(userRepresentation);

        return Response.ok("Password updated successfully").build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response deleteUser(@PathParam("id") String id) {
        keycloak.realm(realm).users().get(id).remove();
        return Response.ok().build();
    }
//
//    public static class User {
//
//        private final String userName;
//
//        User(SecurityIdentity identity) {
//            this.userName = identity.getPrincipal().getName();
//        }
//
//        public String getUserName() {
//            return userName;
//        }
//    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(UserRequest userRequest) {
        String userName = userRequest.getUserName();
        String password = userRequest.getPassword();
        String email = userRequest.getEmail();
        String name = userRequest.getName();
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty username or password").type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        List<UserRepresentation> userRepresentations = keycloak.realm("SELECTION-ENGINE").users().list();
        for (UserRepresentation userRepresentation : userRepresentations) {
            if (userRepresentation.getEmail().equalsIgnoreCase(email) || userRepresentation.getUsername().equalsIgnoreCase(userName)) {
                return Response.status(Response.Status.CONFLICT).entity("User with same email or username already exists").build();
            }
        }
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);
        credentials.setTemporary(false);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userName);
        userRepresentation.setFirstName(name);
        userRepresentation.setLastName(name);
        userRepresentation.setEmail(email);
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(Arrays.asList(credentials));
        userRepresentation.setEmailVerified(false);
        userRepresentation.setRequiredActions(Collections.singletonList(UserModel.RequiredAction.VERIFY_EMAIL.name()));
        Response result = keycloak.realm("SELECTION-ENGINE").users().create(userRepresentation);


        int status = result.getStatus();
        assignRoles(userRepresentation.getId(), Arrays.asList("admin"));

        if (status == 201 || status == 200 || status == 204) {
            userRepresentation = keycloak.realm("SELECTION-ENGINE").users().searchByEmail(email, true).get(0);
            keycloak.realm("SELECTION-ENGINE").users().get(userRepresentation.getId()).sendVerifyEmail();
            return Response.status(status).entity("User created").build();
        }

        //Roles maangement :


        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while creating user").build();
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProfile(UpdateProfileRequest updateProfileRequest) {
        String user = updateProfileRequest.getUser();
        String username = updateProfileRequest.getUsername();
        String email = updateProfileRequest.getEmail();

        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(username) || StringUtils.isEmpty(email)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty username or email").type(MediaType.APPLICATION_JSON_TYPE).build();
        }


        String connectUserName = identity.getPrincipal().getName();
        UserRepresentation userRepresentation = keycloak.realm("SELECTION-ENGINE").users().search(connectUserName).get(0);
        userRepresentation.setUsername(username);
        userRepresentation.setEmail(email);
        userRepresentation.setEmailVerified(false);
        userRepresentation.setFirstName(user);
        userRepresentation.setLastName(user);

        UserResource userResource = keycloak.realm(realm).users().get(userRepresentation.getId());
        userResource.update(userRepresentation);
        userResource.sendVerifyEmail();

        return Response.status(Response.Status.OK).entity("User updated successfully").build();
    }

    private void assignRoles(String userId, List<String> roles) {

        List<RoleRepresentation> roleList = rolesToRealmRoleRepresentation(roles);
        keycloak.realm("SELECTION-ENGINE").users().get(userId).roles().realmLevel().add(roleList);

    }

    private List<RoleRepresentation> rolesToRealmRoleRepresentation(List<String> roles) {

        List<RoleRepresentation> existingRoles = keycloak.realm("SELECTION-ENGINE").roles().list();

        List<String> serverRoles = existingRoles.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
        List<RoleRepresentation> resultRoles = new ArrayList<>();

        for (String role : roles) {
            int index = serverRoles.indexOf(role);
            if (index != -1) {
                resultRoles.add(existingRoles.get(index));
            } else {
                System.out.println("Role does not exist");
            }
        }
        return resultRoles;
    }


    @GET
    @RolesAllowed("admin")
    public Response getUsers() {
        Collection<UserRepresentation> userRepresentationCollection = keycloak.realm("SELECTION-ENGINE").users().list();
        Collection<UserDTO> userDTOS = new ArrayList<>();
        for (UserRepresentation userRepresentation : userRepresentationCollection) {
            Collection<RoleRepresentation> roleRepresentations = keycloak.realm(realm).users().get(userRepresentation.getId()).roles().realmLevel().listAll();
            UserDTO userDTO = new UserDTO(userRepresentation);
            userDTO.setRole(roleService.getRole(roleRepresentations));
            userDTOS.add(userDTO);

        }
        return Response.ok(userDTOS, MediaType.APPLICATION_JSON).build();
    }

    private boolean verifyPassword(String userId, String password) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        List<CredentialRepresentation> credentials = userResource.credentials();
        for (CredentialRepresentation credential : credentials) {
            if (credential.getType().equals(PasswordCredentialModel.TYPE)) {
//                PasswordCredentialModel passwordModel = PasswordCredentialModel.createFromCredentialRepresentation(credential);
//                if (passwordModel != null && passwordModel.getPassword().equals(password)) {
//                    // Passwords match
//                    return true;
//                }
            }
        }
        // Passwords don't match
        return false;
    }

    @Path("/forgot-password")
    @POST
    public Response forgotPassword(@QueryParam("email") String email) {
        UserRepresentation userRepresentation = keycloak.realm(realm).users().searchByEmail(email, true).get(0);
        UserResource userResource = keycloak.realm(realm).users().get(userRepresentation.getId());
        userResource.executeActionsEmail(Collections.singletonList(UserModel.RequiredAction.UPDATE_PASSWORD.name()));
        return Response.accepted().build();
    }

}
