package org.example.aicareernav1.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wishes")
public class WishesViewController {

  @GetMapping("/form")
  public String getWishesForm() {
    return "wishes-page"; // Будет искать src/main/resources/templates/wishes-page.html
  }
}