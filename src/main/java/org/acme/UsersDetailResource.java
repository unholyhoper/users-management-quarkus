package org.acme;

import io.quarkus.security.identity.SecurityIdentity;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import response.UserDTO;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;

@Path("/api/users/bh")

public class UsersDetailResource {



    @Inject
    Keycloak keycloak;




}
