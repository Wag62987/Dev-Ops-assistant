package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Interfaces.CrudService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class AppUserService implements CrudService<AppUser,Long> {
    @Autowired
    AppUserRepository userRepository;


    @Override
    public AppUser Save(AppUser appUser) {
        return userRepository.save(appUser);
    }

    @Override
    public Optional<AppUser> FindById(String id) {
        return  userRepository.findByGithubId(id);
    }
}
