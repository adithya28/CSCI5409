package com.ad368540.ticTacToe.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum GameState {
    WAITING_FOR_PLAYER("Waiting for Guest Player to join game"),
    PLAYER1_TURN("Host Player's turn."),
    PLAYER2_TURN("Guest Player's turn."),
    PLAYER1_WON("Host Player Won"),
    PLAYER2_WON("Guest Player Won"),
    TIE("Tie."),
    GAME_INCOMPLETE("Game Incomplete");
    String description;

}
