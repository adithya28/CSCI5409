package com.ad368540.ticTacToe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;


@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfoModel {
    private String gameID;
    @Getter(onMethod_={@DynamoDbPartitionKey})
    private String sessionID;
}
