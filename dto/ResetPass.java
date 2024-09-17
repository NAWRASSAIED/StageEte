package tn.spring.pispring.dto;

import lombok.Data;

@Data
public class ResetPass {
    String oldPassword;
    String newPassword;
    String code;
}
