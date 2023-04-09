var gameID;
const createGame = () => {
    gameID=generateString(7).trim();
    document.getElementById("gameURL").innerHTML = 'http://ad368540-ta5409/game/'+gameID.trim();
    sendMessage({
        gameID:gameID,
        type: "createGame",
    });
}
const connect = () => {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/game.'+gameID, function (message) {
            const messageData=JSON.parse(message.body);
            console.log(messageData);
        });
    });
}
const sendMessage = (message) => {
    var messageType = message.type
    stompClient.send('/app/'+messageType, {}, JSON.stringify(message));
}
const characters ='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

function generateString(length) {
    let result = ' ';
    const charactersLength = characters.length;
    for ( let i = 0; i < length; i++ ) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

window.onload = function () {
    connect();
}