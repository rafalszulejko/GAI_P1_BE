package com.gauntletai.chat.domain;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gauntletai.chat.domain.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Value("${auth0.domain}")
    private String AUTH0_DOMAIN;

    @Value("${auth0.clientId}")
    private String AUTH0_CLIENT_ID;

    @Value("${auth0.clientSecret}")
    private String AUTH0_CLIENT_SECRET;

    private String getManagementApiToken() throws Auth0Exception {
        try {
            AuthAPI authAPI = new AuthAPI(AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET);
            String audience = "https://" + AUTH0_DOMAIN + "/api/v2/";
            System.out.println("Requesting token for domain: " + AUTH0_DOMAIN);
            System.out.println("Using audience: " + audience);
            
            TokenHolder holder = authAPI.requestToken(audience).execute().getBody();
            String token = holder.getAccessToken();
            System.out.println("Token received: " + (token != null ? "yes" : "no"));
            return token;
        } catch (Auth0Exception e) {
            System.err.println("Auth0 error details: " + e.getMessage());
            throw e;
        }
    }

    public String getUserEmail(DecodedJWT jwt) throws Auth0Exception {
        String managementApiToken = getManagementApiToken();
        ManagementAPI mgmt = new ManagementAPI(AUTH0_DOMAIN, managementApiToken);
        
        String userId = jwt.getClaim("sub").asString();
        com.auth0.json.mgmt.users.User auth0User = mgmt.users().get(userId, null).execute().getBody();
        
        return auth0User.getEmail();
    }

    public User getOrCreateUser(Jwt jwt) {
        String auth0Id = jwt.getSubject();
        try {
            return userRepository.findByAuth0Id(auth0Id)
                    .orElseGet(() -> createNewUser(jwt));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // If we get a duplicate key exception, another request has just created the user
            // Try to fetch it again
            return userRepository.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new IllegalStateException("User was created but cannot be found"));
        }
    }

    private User createNewUser(Jwt jwt) {
        String email;
        try {
            String userId = jwt.getSubject();
            String managementApiToken = getManagementApiToken();
            ManagementAPI mgmt = new ManagementAPI(AUTH0_DOMAIN, managementApiToken);
            com.auth0.json.mgmt.users.User auth0User = mgmt.users().get(userId, null).execute().getBody();
            email = auth0User.getEmail();
        } catch (Auth0Exception e) {
            throw new RuntimeException("Failed to fetch user email from Auth0", e);
        }

        String name = jwt.getClaim("name");
        if (name == null || name.isEmpty()) {
            name = email.split("@")[0];
        }
        
        return createNewUser(name, email, jwt.getSubject(), false);
    }

    public User createNewUser(String name, String email,  String auth0Id, boolean isAi) {
        User user = User.builder()
            .id(UUID.randomUUID().toString())
            .auth0Id(auth0Id)
            .email(email)
            .username(name)
            .createdAt(new Date())
            .lastActive(new Date())
            .isOnline(true)
            .isAi(isAi)
            .build();
        return userRepository.save(user);    
    }

    public User findById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(User.class, id));
    }

    public Optional<User> findByAuth0Id(String auth0Id) {
        return userRepository.findByAuth0Id(auth0Id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> search(String searchTerm) {
        return userRepository.findByIsAiAndUsernameContainingIgnoreCase(false, searchTerm);
    }
} 
