package tn.spring.pispring.Interfaces;


import tn.spring.pispring.dto.OTP;

public interface OTPInterface {
    OTP GenerateOTp( );
    Boolean VerifOTP ( String identification )  ;
    void userstatus(String emailuser, Boolean result);
    OTP ResendOTP(String email);
    void  DeleteALLOTP();
}
