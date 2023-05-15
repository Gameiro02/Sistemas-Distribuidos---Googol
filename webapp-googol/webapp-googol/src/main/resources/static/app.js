var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
});

stompClient.subscribe('/admin/updates', function (AdminInfo) {
    console.log("AdminInfo: " + AdminInfo);
});