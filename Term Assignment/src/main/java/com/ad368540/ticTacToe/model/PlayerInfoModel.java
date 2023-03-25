package com.ad368540.ticTacToe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoModel {
    @Getter(onMethod_={@DynamoDbPartitionKey})
    private String playerName;
    @Getter(onMethod_={@DynamoDbSortKey})
    private String discordName;
    private String gameID;
}
