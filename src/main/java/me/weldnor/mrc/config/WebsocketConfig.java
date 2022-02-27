//package me.weldnor.mrc.config;
//
//import me.weldnor.mrc.controller.ws.MyWebSocketHandler;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebsocketConfig implements WebSocketConfigurer {
//
//    private final MyWebSocketHandler myWebSocketHandler;
//
//    public WebsocketConfig(MyWebSocketHandler myWebSocketHandler) {
//        this.myWebSocketHandler = myWebSocketHandler;
//    }
//
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry
//                .addHandler(myWebSocketHandler, "/ws")
//                .setAllowedOriginPatterns("*"); //TODO fixme
//    }
//}
