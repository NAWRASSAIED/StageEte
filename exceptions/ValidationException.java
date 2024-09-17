package tn.spring.pispring.exceptions;

public class ValidationException extends  RuntimeException{

    public ValidationException(String message){
        super(message);
    }

}
