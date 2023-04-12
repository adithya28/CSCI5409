package com.ad368540.ticTacToe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinMessage {
    private String type;
    private String gameID;
    private String playerName;
    private String discordName;
    private String content;

}
