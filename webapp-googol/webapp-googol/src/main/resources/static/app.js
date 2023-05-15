var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    // $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/admin', function (greeting) {
            var message = JSON.parse(greeting.body).content;
            var formattedMessage = formatMessage(message);
            showGreeting(formattedMessage);
        });
        sendName(); // Envia o nome automaticamente após a conexão
    });
}

// function disconnect() {
//     if (stompClient !== null) {
//         stompClient.disconnect();
//     }
//     setConnected(false);
//     console.log("Disconnected");
// }

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({ 'name': $("#name").val() }));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function formatMessage(message) {
    var lines = message.split('\n');
    var formattedMessage = "";

    lines.forEach(function (line) {
        if (line.includes("Downloader") || line.includes("Barrel") || line.includes("Search")) {
            formattedMessage += "<strong>" + line + "</strong><br>";
        } else {
            formattedMessage += line + "<br>";
        }
    });

    return formattedMessage;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    // $("#disconnect").click(function () { disconnect(); });

    connect(); // Conecta automaticamente ao carregar a página
});
