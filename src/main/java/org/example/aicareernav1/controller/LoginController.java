package org.example.aicareernav1.controller;

import java.util.Date;
import org.example.aicareernav1.dto.user.LoginRequestDto;
import org.example.aicareernav1.model.user.User;
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
      User user = authResult.getUser();
      Long userId = user.getId();
      session.setAttribute("user", authResult.getUser());
      session.setAttribute("userEmail", loginRequest.getEmail());
      session.setAttribute("authenticated", true);
      session.setAttribute("userName", user.getName() != null ? user.getName() : loginRequest.getEmail().split("@")[0]);
      session.setAttribute("roadmapId", user.getRoadmapId());
      session.setAttribute("registrationDate", user.getCreatedAt() != null ? Date.from(user.getCreatedAt()) : new Date());
      return "redirect:/personal-cabinet/" + userId;
    } else {
      model.addAttribute("errors", authResult.getErrors());
      model.addAttribute("email", loginRequest.getEmail());
      return "login";
    }
  }
}