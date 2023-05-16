var stompClient = null;
var isFirstMessage = true; // Variável de controle

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

            if (!isFirstMessage) { // Atualiza o JSON exibido somente se não for a primeira mensagem
                updateJsonOnScreen();
            } else {
                isFirstMessage = false;
            }
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
    $("#greetings").empty(); // Limpa as mensagens anteriores
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}


function formatMessage(message) {
    var lines = message.split('\n');
    var formattedMessage = "";

    try {
        var jsonObject = JSON.parse(message);
        var formattedJSON = JSON.stringify(jsonObject, null, 2);
        formattedMessage += "<pre>" + escapeHtml(formattedJSON) + "</pre>";
    } catch (error) {
        console.log("Failed to parse JSON:", error);
    }

    return formattedMessage;
}

function escapeHtml(html) {
    var div = document.createElement('div');
    div.textContent = html;
    return div.innerHTML;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    // $("#disconnect").click(function () { disconnect(); });

    connect(); // Conecta automaticamente ao carregar a página
});

function updateJsonOnScreen() {
    $.ajax({
        url: '/api/getJson', // URL para obter o JSON atualizado
        method: 'GET',
        success: function (response) {
            $("#jsonDisplay").html(response); // Atualiza o JSON exibido na tela
        },
        error: function (error) {
            console.log('Failed to update JSON:', error);
        }
    });
}
