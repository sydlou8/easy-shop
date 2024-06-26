package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController extends UserBase{
    private ProfileDao profileDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        super(userDao);
        this.profileDao = profileDao;
    }
    @GetMapping({"", "/"})
    Profile getProfile(Principal principal) {
        return profileDao.getProfile(getUserId(getUser(principal)));
    }
    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public Profile createProfile(@RequestBody Profile profile) {
        return profileDao.create(profile);
    }
    @PutMapping({"", "/"})
    @ResponseStatus(HttpStatus.OK)
    public void updateProfile(Principal principal, @RequestBody Profile profile) {
        int userId = getUserId(getUser(principal));
        profileDao.update(userId, profile);
    }
}
