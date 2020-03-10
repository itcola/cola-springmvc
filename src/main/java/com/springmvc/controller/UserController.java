package com.springmvc.controller;


import com.springmvc.annotation.ColaAutowired;
import com.springmvc.annotation.ColaController;
import com.springmvc.annotation.ColaParam;
import com.springmvc.annotation.ColaRequestMapping;
import com.springmvc.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ColaController
@ColaRequestMapping("/cola")
public class UserController {

    @ColaAutowired("UserServiceImpl")
    private UserService userService;


    @ColaRequestMapping("/write")
    public void colaWrite(HttpServletRequest request, HttpServletResponse response, @ColaParam("username") String username, @ColaParam("password") String password){
        try {
            PrintWriter pw = response.getWriter();
            String result = userService.colaWrite(username,password);
            pw.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
