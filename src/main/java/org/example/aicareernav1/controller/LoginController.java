package org.example.aicareernav1.controller;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.example.aicareernav1.dto.user.LoginRequestDto;
import org.example.aicareernav1.service.user.UserService;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;

  @GetMapping("/login")
  public String showLoginForm(@RequestParam(value = "registered", required = false) String registered,
      @RequestParam(value = "email", required = false) String email,
      Model model) {
    model.addAttribute("loginRequest", new LoginRequestDto());
    if (registered != null) {
      model.addAttribute("registered", true);
      model.addAttribute("registeredEmail", email);
    }
    return "login"; // /jsp/login.jsp
  }

  @PostMapping("/login")
  public String processLogin(@Valid LoginRequestDto loginRequest,
      BindingResult result,
      HttpSession session,
      Model model) {
    if (result.hasErrors()) {
      return "login";
    }

    AuthenticationResult authResult = userService.authenticateUser(loginRequest);
    if (authResult.isSuccess()) {
      session.setAttribute("user", authResult.getUser());
      session.setAttribute("userEmail", loginRequest.getEmail());
      session.setAttribute("authenticated", true);
      session.setAttribute("userName", loginRequest.getEmail().split("@")[0]);
      session.setAttribute("registrationDate", new Date());
      return "redirect:/personal-cabinet";
    } else {
      model.addAttribute("errors", authResult.getErrors());
      model.addAttribute("email", loginRequest.getEmail());
      return "login";
    }
  }
}