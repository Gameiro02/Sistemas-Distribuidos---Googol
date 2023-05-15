var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
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
        stompClient.subscribe('/topic/greetings', function (greeting) {
            var message = JSON.parse(greeting.body).content;
            var formattedMessage = formatMessage(message);
            showGreeting(formattedMessage);
        });
        sendName(); // Envia o nome automaticamente após a conexão
    });
}

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

function updateMessages() {
    $.ajax({
        url: 'url_do_servidor', // Substitua pela URL que retorna as mensagens atualizadas
        method: 'GET', // Use GET, POST ou outro método apropriado
        success: function (response) {
            // Manipule a resposta recebida do servidor
            // Verifique se há novas mensagens e adicione-as à lista de saudações
            response.forEach(function (message) {
                var formattedMessage = formatMessage(message);
                showGreeting(formattedMessage);
            });
        },
        error: function (error) {
            // Manipule erros, se houver
        }
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    connect(); // Conecta automaticamente ao carregar a página

    // Chama a função de atualizar mensagens em intervalos regulares
    setInterval(updateMessages, 5000); // 5000 milissegundos = 5 segundos
});
