package tn.spring.pispring.config.JWT.Controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.spring.pispring.Interfaces.OTPInterface;
import tn.spring.pispring.dto.OTP;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/OTP")
public class OTPController {

    private final OTPInterface otpInterface;

    @PostMapping("/GenerateOTp")
    public OTP GenerateOTp() {
        return otpInterface.GenerateOTp();
    }

    @PostMapping("/VerifOTP/{identification}")
    public Boolean VerifOTP(@PathVariable("identification") String identification) {
        return otpInterface.VerifOTP(identification);
    }

    @PostMapping("/userstatus/{emailuser}/{result}")
    public void userstatus(@PathVariable("emailuser") String emailuser, @PathVariable("result") Boolean result) {
        otpInterface.userstatus(emailuser, result);
    }

    @PostMapping("/ResendOTP/{email}")
    public OTP ResendOTP(@PathVariable("email") String email) {
        return otpInterface.ResendOTP(email);
    }

    @DeleteMapping("/DeleteALLOTP")
    public void DeleteALLOTP() {
        otpInterface.DeleteALLOTP();
    }
}

