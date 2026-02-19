package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.AppUser;

import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.UserDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Utility.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
 private final AppUserService userService;
 private final AuthenticatedUser authenticatedUser;

 @GetMapping("/Info")
 public ResponseEntity<UserDTO> getMethodName() {
    String githubId =authenticatedUser.getCurrentUserGithubId();
    UserDTO user=userService.GetUserInfo(userService.FindById(githubId));
    return ResponseEntity.ok(user);
 }
}