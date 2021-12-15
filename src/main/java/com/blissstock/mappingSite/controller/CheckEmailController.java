package com.blissstock.mappingSite.controller;

import com.blissstock.mappingSite.dto.EmailCheckRegisterDTO;
import com.blissstock.mappingSite.service.UserService;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckEmailController {

  private static final Logger logger = LoggerFactory.getLogger(CheckEmailController.class);

  @Autowired
  UserService userService;

  /// A Get Method For Email Check Before Register
  @GetMapping(path = { "/check_email/register/{role}" })
  public String registerForm(
      @PathVariable(name = "role", required = false) String role,
      Model model,
      String email) {
    logger.info("GET Request");
    logger.info("Role is {}, email is {}", role, email);
    // Tell Thymeleaf to render as Reister
    model.addAttribute("action", "register");

    // For Post Method Action
    model.addAttribute("postAction", "/check_email/register/");

    /*
     * if (email != null && !email.isBlank()) {
     * EmailValidator emailValidator = new EmailValidator();
     * boolean isValidEmail = emailValidator.validateEmail(email);
     * if (isValidEmail) {
     * model.addAttribute("email", email);
     * }
     * }
     */

    // Initialize Form
    if (role == null || !role.equals("teacher")) {
      role = "student";
    }
    logger.debug("Role is {}, ", role);

    EmailCheckRegisterDTO emailCheckRegisterDTO = new EmailCheckRegisterDTO();
    emailCheckRegisterDTO.setEmail(email);
    emailCheckRegisterDTO.setRole(role);
    model.addAttribute("emailCheck", emailCheckRegisterDTO);
    logger.debug("email  checked {}", email);
    // render
    return "ST0000_check_email";
  }

  /// A Post Method for email check before Register
  @PostMapping("/check_email/register")
  public String register(
      Model model,
      @Valid @ModelAttribute("emailCheck") EmailCheckRegisterDTO emailRegister,
      BindingResult bindingResult) {
    logger.info("POST Resquest");
    if (bindingResult.hasErrors()) {
      logger.warn("Invalid Form Field error {},count", bindingResult.getFieldError());
      model.addAttribute("action", "register");

      /*
       * // For Rendering the title
       * model.addAttribute("role", emailRegister.getRole());
       */
      // render
      return "ST0000_check_email.html";
    }
    if (userService.getUserAccountByEmail(emailRegister.getEmail()) != null) {
      logger.warn("user with {} email already exists", emailRegister.getEmail());
      // User already exists
      model.addAttribute("action", "register");
      /*
       * // For Rendering the title
       * model.addAttribute("role", emailRegister.getRole());
       */
      // For displaying error message
      model.addAttribute("error", true);

      // render
      return "ST0000_check_email.html";
    }
    // Redirect to Register
    // Two Valid Address:
    // 1. /register/student/email@gmail.com
    // 2. /register/teacher/email@gmail.com
    logger.info("Valided role is {}, email is {}", emailRegister.getRole(), emailRegister.getEmail());
    return ("redirect:/register/" +
        emailRegister.getRole() +
        "/" +
        emailRegister.getEmail() +
        "/");
  }

  /// A Get Method For Email Check Before Register
  @GetMapping(path = { "/check_email/reset_password" })
  public String passwordResetForm(Model model, String error, String email) {
    logger.info("GET Request");
    // Tell Thymeleaf to render as Reister
    model.addAttribute("action", "verify_password");
    EmailCheckRegisterDTO dto = new EmailCheckRegisterDTO();
    if (email != null) {
      dto.setEmail(email);
    }
    // For Post Method Action
    model.addAttribute("postAction", "/password/reset_password/");
    model.addAttribute("emailCheck", dto);

    model.addAttribute("passwordResetError", error);

    // render
    return "ST0000_check_email";
  }

  @PostMapping(path = { "/check_email/reset_password" })
  public String passwordReset(Model model, BindingResult bindingResult) {
    logger.info("POST Request");
    // Tell Thymeleaf to render as Reister
    if (bindingResult.hasErrors()) {
      model.addAttribute("action", "verify_password");

      // For Post Method Action
      model.addAttribute("postAction", "/check_email/reset_password/");
      model.addAttribute("emailCheck", new EmailCheckRegisterDTO());

      // render
      return "ST0000_check_email";
    }
    return "redirect:/password/reset_password";
  }
}
