'use strict';

let ws = new SockJS('http://localhost:8080/ws');
let stomp = Stomp.over(ws);
stomp.debug = null // disable stomp logs

stomp.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stomp.subscribe('/topic/message', function (greeting) {
        console.log(greeting.body);
    });
    stomp.send("/app/message", {}, "hello");
});
