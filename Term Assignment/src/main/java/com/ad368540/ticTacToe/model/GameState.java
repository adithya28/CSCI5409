package com.ad368540.ticTacToe.model;


public enum GameState {
    WAITING_FOR_PLAYER("Waiting for player to join game"),
    PLAYER1_TURN("Host Player's turn."),
    PLAYER2_TURN("Guest Player's turn."),
    PLAYER1_WON("Player 1 won."),
    PLAYER2_WON("Player 2 won."),
    TIE("Tie."),
    GAME_INCOMPLETE("Incomplete");

    String description;

    GameState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
