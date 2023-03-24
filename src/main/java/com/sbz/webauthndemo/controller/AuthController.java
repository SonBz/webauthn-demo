package com.sbz.webauthndemo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.repository.UserRepository;
import com.sbz.webauthndemo.service.LoginService;
import com.sbz.webauthndemo.service.RegisterService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final UserRepository userRepository;

    AuthController(RegisterService registerService, LoginService loginService, UserRepository userRepository) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @ResponseBody
    public String newUserRegistration(@RequestParam String username, @RequestParam String displayname, HttpSession session) {
        AppUser saveUser = registerService.newUser(username, displayname);
        return registerService.newPublicKey(saveUser, session);
    }

    @PostMapping("/registerauth")
    @ResponseBody
    public String newAuthRegistration(@RequestParam AppUser user, HttpSession session) {
        return registerService.newPublicKey(user, session);
    }

    @PostMapping("/finishauth")
    @ResponseBody
    public ModelAndView finishRegisration(@RequestParam String credential, @RequestParam String username, HttpSession session) {
        registerService.newAuth(credential, username, session);
        return new ModelAndView("redirect:/mfa?username="+ username, HttpStatus.SEE_OTHER);
    }


    @PostMapping("/login")
    @ResponseBody
    public String startLogin(@RequestParam String username, HttpSession session) {
        AssertionRequest request = loginService.startLogin(username);
        try {
            session.setAttribute(username, request);
            return request.toCredentialsGetJson();
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/check/username")
    public String finishLogin(@RequestParam String credential, @RequestParam String username, Model model, HttpSession session) {
        AssertionResult result = loginService.finishLogin(credential, username, session);
        if (result.isSuccess()) {
            model.addAttribute("name", username);
            userRepository.findByUsername(username)
                    .ifPresent(appUser -> model.addAttribute("displayName", appUser.getDisplayName()));
            return "mfa";
        } else {
            return "index";
        }
    }

    @GetMapping("/")
    public String welcome() {
        return "index";
    }

    @GetMapping("/register")
    public String registerUser(Model model) {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


}
