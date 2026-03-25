package org.example.aicareernav1.controller.view;

import org.example.aicareernav1.enums.DialogType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dialogs")
public class DialogViewController {

  @GetMapping("/chat")
  public String getChatPage(@RequestParam Long userId,
                            @RequestParam DialogType type,
                            Model model) {
    // Передаем параметры в HTML, чтобы JavaScript знал, к кому обращаться
    model.addAttribute("userId", userId);
    model.addAttribute("dialogType", type);
    return "chat"; // Будет искать src/main/resources/templates/chat.html
  }
}
