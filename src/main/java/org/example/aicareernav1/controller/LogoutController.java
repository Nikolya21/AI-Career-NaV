package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    if (session != null) {
      session.invalidate();
    }
    return "redirect:/login";
  }
}