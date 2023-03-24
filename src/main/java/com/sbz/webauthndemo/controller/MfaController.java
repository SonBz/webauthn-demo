package com.sbz.webauthndemo.controller;

import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.repository.UserRepository;
import com.sbz.webauthndemo.service.MfaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;

    public MfaController(MfaService mfaService, UserRepository userRepository) {
        this.mfaService = mfaService;
        this.userRepository = userRepository;
    }

    @GetMapping("/mfa")
    public void setup(@RequestParam String username, Model model){
        String qrcode = mfaService.createQR(username);
        model.addAttribute("qrcode", qrcode);
        model.addAttribute("name", username);
    }

    @PostMapping("/mfa/verify")
    @ResponseBody
    public ModelAndView verify(@RequestParam String code,
                               @RequestParam String displayName,
                               @RequestParam String username,
                               Model model) {
        if (mfaService.verify(code, username)) {
            model.addAttribute("displayname", displayName);
            return new ModelAndView("redirect:/welcome", HttpStatus.SEE_OTHER);
        }
        return new ModelAndView("redirect:/mfa?username="+ username, HttpStatus.SEE_OTHER);
    }
}
