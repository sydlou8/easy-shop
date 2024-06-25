package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.yearup.data.UserDao;
import org.yearup.models.User;

import java.security.Principal;

public abstract class UserBase {
    protected UserDao userDao;

    @Autowired
    UserBase (UserDao userDao) {
        this.userDao = userDao;
    }

    User getUser (Principal principal) {
        // get the currently logged in username
        String userName = principal.getName();
        // find database user by userId
        return userDao.getByUserName(userName);
    }

    int getUserId(User user) {
        return user.getId();
    }
}
