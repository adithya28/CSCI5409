let stompClient = null;
let game = null;
let player = null;

/**
 * Sends a message to the server using the STOMP client.
 * @param {Object} message - The message to be sent. Must contain at least a "type" field.
 */
const sendMessage = (message) => {
    var messageType = message.type
    stompClient.send('/app/'+ messageType, {}, JSON.stringify(message));
}

/**
 * Sends a move message to the server.
 * @param {Number} move - The index of the cell where the move should be made.
 */
const makeMove = (move) => {
    sendMessage({
        type: "makeMove",
        move: move,
        turn: game.turn,
        sender: player,
        gameID: game.gameID
    });
}

/**
 * An object containing functions to handle each type of message received from the server.
 */
const messagesTypes = {
    "joinGame": (message) => {
        updateGame(message);
    },
    "gameOver": (message) => {
        updateGame(message);
        if (message.gameState === 'TIE') toastr.success(`Game over! It's a tie!`);
        else showWinner(message.winner);
    },
    "game.joined": (message) => {
        if (game !== null && game.gameID !== message.gameID) return;
        player = localStorage.getItem("playerName");

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect(message.gameID,function (frame) {
            stompClient.subscribe(`/topic/game.${message.gameID}`, function (message) {
                handleMessage(JSON.parse(message.body));
            });
        });
        updateGame(message);
    },
    "makeMove": (message) => {
        updateGame(message);
    },
    "leaveGame": (message) => {
        updateGame(message);
        if (message.winner) showWinner(message.winner);
    },
    "createGame": (message) => {
        updateGame(message);
        if (message.winner) showWinner(message.winner);
    },
    "error": (message) => {
        toastr.error(message.content);
    }
}

/**
 * Handles a message received from the server.
 * @param {Object} message - The message received.
 */
const handleMessage = (message) => {
    if (messagesTypes[message.type])
        messagesTypes[message.type](message);
}

/**
 * Converts a message received from the server into a game object.
 */
const messageToGame = (message) => {
    return {
        gameID: message.gameID,
        gameBoard: message.gameBoard,
        turn: message.turn,
        hostPlayer: message.hostPlayer,
        guestPlayer: message.guestPlayer,
        gameState: message.gameState,
        winner: message.winner
    }
}

/**
 * Displays a success message with the name of the winning player.
 * @param {String} winner - The name of the winning player.
 */
const showWinner = (winner) => {
    toastr.success(`The winner is ${winner}!`);
    const winningPositions = getWinnerPositions(game.board);
    if (winningPositions && winningPositions.length === 3) {
        winningPositions.forEach(position => {
            const row = Math.floor(position / 3);
            const cell = position % 3;
            let cellElement = document.querySelector(`.row-${row} .cell-${cell} span`);
            cellElement.style.backgroundColor = 'grey';
        });
    }
}

/**
 * Starts the process of joining a game. Asks the player to enter their name and sends a message to the server requesting to join the game.
 */
const joinGame = () => {
    var currentGameID = window.location.href.trim().substring(27,34);
    const playerName = localStorage.getItem("playerName");
    sendMessage({
        type: "joinGame",
        playerName: playerName,
        gameID:currentGameID
    });
}

/**
 * Connects the STOMP client to the server and subscribes to the "/topic/game.state" topic.
 */
const connect = () => {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/updateGameState', function (message) {
            handleMessage(JSON.parse(message.body));
        });
        loadGame();
    });
}

/**
 * Attempts to load a game by joining with the player's previously stored name, or prompts the player to enter their name if no name is stored.
 */
const loadGame = () => {
    let playerName = localStorage.getItem("playerName");
    if (!playerName) {
        playerName=document.getElementById(#username);
       localStorage.setItem("playerName", playerName);
    }
    joinGame();
 }

/**
 * Updates the game state with the information received from the server.
 * @param {Object} message - The message received from the server.
 */
const updateGame = (message) => {
    game = messageToGame(message);
    updateBoard(message.gameBoard);
    document.getElementById("hostPlayer").innerHTML = game.hostPlayer;
    document.getElementById("guestPlayer").innerHTML =game.guestPlayer || (game.winner ? '-' : 'Waiting for player 2...');
    document.getElementById("turn").innerHTML = game.turn;
    document.getElementById("winner").innerHTML = game.winner || '-';
}

/**
 * Updates the game board with the information received from the server.
 * @param {Array} board - The board received from the server.
 */
const updateBoard = (board) => {
    let counter = 0;
    board.forEach((row, rowIndex) => {
        row.forEach((cell, cellIndex) => {
            const cellElement = document.querySelector(`.row-${rowIndex} .cell-${cellIndex}`);
            cellElement.innerHTML = cell === ' ' ? '<button onclick="makeMove(' + counter + ')"> </button>' : `<span class="cell-item">${cell}</span>`;
            counter++;
        });
    });
}

/**
 * Get the winner positions from the board.
 * @param {Array} board - The board received from the server.
 */
const getWinnerPositions = (board) => {
    const winnerPositions = [];

    for (let i = 0; i < 3; i++) {
        if (board[i][0] === board[i][1] && board[i][1] === board[i][2] && board[i][0] !== ' ') {
            winnerPositions.push(i * 3);
            winnerPositions.push(i * 3 + 1);
            winnerPositions.push(i * 3 + 2);
        }
    }

    for (let i = 0; i < 3; i++) {
        if (board[0][i] === board[1][i] && board[1][i] === board[2][i] && board[0][i] !== ' ') {
            winnerPositions.push(i);
            winnerPositions.push(i + 3);
            winnerPositions.push(i + 6);
        }
    }

    if (board[0][0] === board[1][1] && board[1][1] === board[2][2] && board[0][0] !== ' ') {
        winnerPositions.push(0);
        winnerPositions.push(4);
        winnerPositions.push(8);
    }

    return winnerPositions;
}

window.onload = function () {
    connect();
}
