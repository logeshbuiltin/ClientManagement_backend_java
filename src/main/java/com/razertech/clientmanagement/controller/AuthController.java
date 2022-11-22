package com.razertech.clientmanagement.controller;


import com.razertech.clientmanagement.dto.ClientDto;
import com.razertech.clientmanagement.entity.Client;
import com.razertech.clientmanagement.repository.EmailDetails;
import com.razertech.clientmanagement.repository.SMS;
import com.razertech.clientmanagement.service.ClientService;
import com.razertech.clientmanagement.service.EmailService;
import com.razertech.clientmanagement.service.OtpGeneratorService;
import com.razertech.clientmanagement.service.SMSService;
import com.twilio.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AuthController {

    int otpLength = 4;

    private OtpGeneratorService otpGenerator = new OtpGeneratorService();

    @Autowired
    private EmailService emailService;

    @Autowired
    SMSService service;

    @Autowired
    private SimpMessagingTemplate webSocket;

    private final String  TOPIC_DESTINATION = "/topic/sms";

    private ClientService userService;

    public AuthController(ClientService userService) {
        this.userService = userService;
    }

    @GetMapping("index")
    public String home(){
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    /**
     * @param model
     * @return
     */
    // handler method to handle user registration request
    @GetMapping("register")
    public String showRegistrationForm(Model model){
        ClientDto user = new ClientDto();
        model.addAttribute("user", user);
        return "register";
    }

    /**
     * @param user
     * @param result
     * @param model
     * @return
     */
    // handler method to handle register user form submit request
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") ClientDto user,
                               BindingResult result,
                               Model model){
        Client existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        userService.saveUser(user);
        return "redirect:/register?success";
    }

    /**
     * @param model
     * @return
     */
    @GetMapping("/users")
    public String listRegisteredUsers(Model model){
        List<ClientDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    /**
     * @param userEmail
     * @return
     */
    @PutMapping("/generateOtp/email")
    public String generatePasswordOtp(@RequestParam("userEmail") String userEmail) {
        Client existing = userService.findByEmail(userEmail);
        if(existing != null) {
            existing.setOtpNo(otpGenerator.OTP(otpLength));
            userService.updateUser(existing);
            EmailDetails details = new EmailDetails();
            details.setRecipient(existing.getEmail());
            details.setSubject("Password reset OTP");
            details.setMsgBody("OTP to change your password: " + existing.getOtpNo());
            String status = emailService.sendSimpleMail(details);
            return "Success: opt generated" + status;
        } else {
            return "Error: opt not generated";
        }
    }

    @RequestMapping(
            value = "/generateOtp/number",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String generatePasswordOtp(@RequestBody SMS sms) {
        Client existing = userService.findByContact(sms.getTo());
        if(existing != null) {
            int genOtp = otpGenerator.OTP(otpLength);
            existing.setOtpNo(genOtp);
            userService.updateUser(existing);
            try{
                sms.setMessage(genOtp+"");
                service.send(sms);
            }
            catch(ApiException e){

                webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() + ": Error sending the SMS: "+e.getMessage());
                throw e;
            }
            webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() + ": SMS has been sent!: "+sms.getTo());
            return "message sent";
        } else {
            return "Error: opt not generated";
        }
    }

    @GetMapping("/validateemailotp")
    public String validateOtp(@RequestParam(required = false) String userEmail, @RequestParam("userOtp") String userOtp) {
        Client existing = userService.findByEmail(userEmail);
        if(existing != null) {
            if(userOtp.equals(existing.getOtpNo())) {
                return "Valid";
            } else {
                return "Invalid";
            }
        } else {
            return "Invalid user email";
        }
    }

    private String getTimeStamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    }
}
