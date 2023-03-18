package com.ad368540.ticTacToe.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TicTacToeMessage {
    private String type;
    private String gameID;
    private String hostPlayer;
    private String guestPlayer;
    private String winner;
    private String turn;
    private String content;
    private String[][] gameBoard;
    private int move;
    private GameState gameState;
    private String sender;
    public TicTacToeMessage(GameInfoModel game) {
        this.gameID = game.getGameID();
        this.hostPlayer = game.getHostPlayer();
        this.guestPlayer = game.getGuestPlayer();
        this.winner = game.getWinner();
        this.turn = game.getCurrentTurn();
        this.gameBoard = game.fetchGameBoard();
        this.gameState = game.getGameState();
    }

}
