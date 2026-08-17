package com.gamehub.exception;
import java.util.List;
//for returning a appropriate error or exception

//{
//  "title": "Your request parameters didn't validate.",
//  "status": 400,
//  "detail": "The 'email' field must be a valid email address.",
//  "instance": "/orders/12345/items",
//  "errors": [
//    {
//      "field": "email",
//      "message": "Invalid email format."
//    }
//  ]
//}

public class ErrorResponse {

    private String title;
    private int status;
    private String instance;
    private String msg;
    private List<ValidationError> errors;

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public ErrorResponse(){

    }

    public ErrorResponse(String title , int status , String instance , String msg){
        this.title=title;
        this.status=status;
        this.instance=instance;
        this.msg=msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
