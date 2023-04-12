package com.ad368540.ticTacToe.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerMessage {
    private String type;
    private String gameID;
    private String playerName;
    private String content;
}
