package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomSuccesHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    AppUserService service;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String,Object> attribute=token.getPrincipal().getAttributes();
        String username=String.valueOf(attribute.get("login"));
        String name=String.valueOf(attribute.get("name"));
        String email=String.valueOf(attribute.get("email"));
        String githubId=String.valueOf(attribute.get("id"));
        Optional<AppUser> existedUser=service.FindById(githubId);
        if(existedUser.isEmpty()){
            AppUser user=new AppUser(username,name,email,githubId);
            service.Save(user);
        }
        response.sendRedirect("/");

    }
}
