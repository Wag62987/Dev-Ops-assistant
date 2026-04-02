package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Config.Jwt.JwtUtil;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        try {
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();

            // 🔍 DEBUG LOG
            log.info("GitHub Attributes: {}", attributes);

   
            Object idObj = attributes.get("id");
            Object loginObj = attributes.get("login");
            Object nameObj = attributes.get("name");

            if (idObj == null || loginObj == null) {
                throw new RuntimeException("GitHub user data missing");
            }

            String githubId = idObj.toString();
            String username = loginObj.toString();
            String name = nameObj != null ? nameObj.toString() : username;

 
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authToken.getAuthorizedClientRegistrationId(),
                    authToken.getName()
            );

            String githubAccessToken = null;

            if (client != null && client.getAccessToken() != null) {
                githubAccessToken = client.getAccessToken().getTokenValue();
            } else {
                log.warn("GitHub access token is null!");
            }

            Optional<AppUser> existingUser = userService.FindById(githubId);

            AppUser user;
            if (existingUser.isEmpty()) {
                user = new AppUser();
                user.setGithubId(githubId);
                user.setUsername(username);
                user.setName(name);
            } else {
                user = existingUser.get();
            }

            if (githubAccessToken != null) {
                user.setGithub_token(githubAccessToken);
            }

            userService.Save(user);

            log.info("User saved successfully: {}", username);

            String jwtToken = jwtUtil.generateToken(user);

            log.info("Generated JWT for {}: {}", username, jwtToken);

        
            ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", jwtToken)
                    .httpOnly(false)   // if you want JS access (change to true for more security)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.setHeader("Authorization", "Bearer " + jwtToken);

            log.info("JWT set in cookie & header");

         
            response.sendRedirect("https://devsopsopera.netlify.app//dashboard");

        } catch (Exception e) {
            log.error("OAuth Success Handler Failed ❌", e);
            throw e; // important → lets Spring show real error
        }
    }
}
