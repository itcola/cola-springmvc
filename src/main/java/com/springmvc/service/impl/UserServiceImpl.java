package com.springmvc.service.impl;

import com.springmvc.annotation.ColaService;
import com.springmvc.service.UserService;

@ColaService("UserServiceImpl")
public class UserServiceImpl implements UserService {
    public String colaWrite(String username,String password) {
        return "name:"+username+"&password:"+password;
    }
}
