package com.ad368540.ticTacToe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoModel {
    private String playerName;
    private String discordName;
    private String gameID;
}
