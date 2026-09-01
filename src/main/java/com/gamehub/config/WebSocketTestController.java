package com.gamehub.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {

    @MessageMapping("/test") // /app/test
    @SendTo("/topic/test") //Whatever this method returns, publish it to this STOMP destination
    public String testMessage(String message){
        return "Server received: " + message;
    }

//    /app/test
//     ↓
//    testMessage()
//     ↓
//    "Server received: Hello"
//     ↓
//     /topic/test
//
}
