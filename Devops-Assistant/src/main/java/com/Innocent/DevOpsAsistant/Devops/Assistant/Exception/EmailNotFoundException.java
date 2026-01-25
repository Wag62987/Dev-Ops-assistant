package com.Innocent.DevOpsAsistant.Devops.Assistant.Exception;

import org.springframework.stereotype.Component;

public class EmailNotFoundException extends Exception{

    public EmailNotFoundException(String msg){
        super(msg);
    }
}
