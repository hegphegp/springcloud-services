package com.hegp.controller;

import com.hegp.bean.ResponseBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @GetMapping(value = "/login")
    public String defaultLogin (HttpServletRequest request, Model model) {
        // 已经登录过了
        String basePath = (String) request.getAttribute("basePath");
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.hasText(username)) {
            return "redirect:"+basePath+"/chat";
        }

        return "login";
    }

    @GetMapping(value = "/getBasePath")
    @ResponseBody
    public ResponseBean getBasePath(HttpServletRequest request) {
        String basePath = (String) request.getAttribute("basePath");
        return new ResponseBean(0, basePath, "success");
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public ResponseBean login(HttpServletRequest request, @RequestParam("name") String username, @RequestParam("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        if (!"tom".equals(username) && !"jack".equals(username)) {
            return new ResponseBean(-1, "", "未知账户");
        }
        if (!"admin".equals(password)) {
            return new ResponseBean(-2, "", "密码不正确");
        }
        request.getSession().setAttribute("username", username);
        String basePath = (String) request.getAttribute("basePath");
        return new ResponseBean(0, basePath+"/chat", "登录成功");
    }
}
