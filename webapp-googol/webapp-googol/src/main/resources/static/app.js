var ws = new WebSocket("/ws");

function sendMessage() {
    var message = "Hello World";
    ws.send("/app/message", {}, JSON.stringify({'content': message}));
}

function showMessage(message) {
    console.log(message);
} 