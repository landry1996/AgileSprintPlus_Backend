package com.agilesprintplus.agilesprint.ws;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BoardEventsController {
    private final SimpMessagingTemplate broker;
    public BoardEventsController(SimpMessagingTemplate broker){
        this.broker=broker;
    }
    @MessageMapping("/echo")
    public void echo(String msg){
        broker.convertAndSend("/topic/updates", msg);
    }
}