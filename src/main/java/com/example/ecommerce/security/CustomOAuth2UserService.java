package com.example.ecommerce.security;

import com.example.ecommerce.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OAuthAccountRepository oauthAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = oauth2User.getAttribute("email");
        String providerUserId = oauth2User.getAttribute("sub");
        if (providerUserId == null) {
            providerUserId = oauth2User.getName();
        }

        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");
        String picture = oauth2User.getAttribute("picture");

        User user = processOAuthUser(registrationId, providerUserId, email, givenName, familyName, picture);
        return CustomUserPrincipal.create(user, oauth2User.getAttributes());
    }

    private User processOAuthUser(String provider, String providerUserId, String email,
                                  String firstName, String lastName, String picture) {
        Optional<OAuthAccount> oauthAccountOptional = oauthAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        if (oauthAccountOptional.isPresent()) {
            User user = oauthAccountOptional.get().getUser();
            // Update profile info if needed
            if (picture != null && user.getProfileImageUrl() == null) {
                user.setProfileImageUrl(picture);
                userRepository.save(user);
            }
            return user;
        }

        // Check if user with same email exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // New user registration - ONLY default to ROLE_CUSTOMER
            Role customerRole = roleRepository.findByName(Role.ROLE_CUSTOMER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(Role.ROLE_CUSTOMER).build()));

            user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .profileImageUrl(picture)
                    .status("ACTIVE")
                    .roles(Collections.singleton(customerRole))
                    .build();

            user = userRepository.save(user);
            log.info("Created new user via OAuth: email={}, id={}", email, user.getId());
        }

        // Link OAuth account
        OAuthAccount account = OAuthAccount.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
        oauthAccountRepository.save(account);

        return user;
    }
}
