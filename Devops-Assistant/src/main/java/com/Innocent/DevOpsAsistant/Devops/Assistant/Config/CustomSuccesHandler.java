package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Component
public class CustomSuccesHandler extends SimpleUrlAuthenticationSuccessHandler {

    
    private final AppUserService service;
    private final OAuth2AuthorizedClientService authorizedClientService;
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String,Object> attribute=token.getPrincipal().getAttributes();
        String username=String.valueOf(attribute.get("login"));
        String name=String.valueOf(attribute.get("name"));
        // String email=String.valueOf(attribute.get("email"));
        String githubId=String.valueOf(attribute.get("id"));
         // 2. Get GitHub OAuth2 access token
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
        );

        String githubToken = client.getAccessToken().getTokenValue();
        System.out.println("GitHub Access Token: " + githubToken);
        Optional<AppUser> existedUser=service.FindById(githubId);
        
        if(existedUser.isEmpty()){
            AppUser user=new AppUser(githubId,name,username,githubToken);
            service.Save(user);
        }else{
            if(!existedUser.get().getGithub_token().equals(githubToken)){
                AppUser user=existedUser.get();
                user.setGithub_token(githubToken);
                service.Save(user);
            }
        }
        
        response.sendRedirect("/github/userRepos");

    }
}
