package com.googlecode.sslplugin.example;


import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.googlecode.sslplugin.annotation.Secured;



public class HelloWorldAction extends ActionSupport {


    private String name;

    @RequiredStringValidator(message = "Please enter a name", trim = true)
    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }


    public String execute() throws Exception {
        return SUCCESS;
    }

    @Secured
    public String execute1() throws Exception {
        return SUCCESS;
    }
}
