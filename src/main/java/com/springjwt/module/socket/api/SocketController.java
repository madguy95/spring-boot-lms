package com.springjwt.module.socket.api;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

//@RestController("/api/socket")
@RequiredArgsConstructor
public class SocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/news")
    public void broadcastNews(@Payload String message) {
        this.simpMessagingTemplate.convertAndSend("/topic/news", message);
    }

    @Scheduled(fixedRate = 1000)
    public void scheduleFixedDelayTask() {
        this.simpMessagingTemplate.convertAndSend("/topic/greetings", 100);
    }
}
