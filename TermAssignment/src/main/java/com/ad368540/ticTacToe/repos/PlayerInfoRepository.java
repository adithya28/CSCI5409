package com.ad368540.ticTacToe.repos;

import com.ad368540.ticTacToe.model.PlayerInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

@Repository
public class PlayerInfoRepository {
    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public void save(PlayerInfoModel playerInfoModel)
    {
        DynamoDbTable<PlayerInfoModel> playerInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("PlayerInfo", TableSchema.fromBean(PlayerInfoModel.class));
        playerInfoModelDynamoDbTable.putItem(playerInfoModel);
    }
    public void updateModel(PlayerInfoModel playerInfoModel)
    {
        DynamoDbTable<PlayerInfoModel> playerInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("PlayerInfo", TableSchema.fromBean(PlayerInfoModel.class));
        playerInfoModelDynamoDbTable.updateItem(playerInfoModel);

    }
    public PlayerInfoModel findPlayerInfoByName(String playerName)
    {
        DynamoDbTable<PlayerInfoModel> playerInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("PlayerInfo", TableSchema.fromBean(PlayerInfoModel.class));
        Key key = Key.builder()
                .partitionValue(playerName)
                .build();
        PlayerInfoModel currentGame = playerInfoModelDynamoDbTable.getItem(key);
        if(currentGame!=null)
            return playerInfoModelDynamoDbTable.getItem(key);
        return null;
    }
    public PageIterable<PlayerInfoModel> getAllPlayers()
    {
        DynamoDbTable<PlayerInfoModel> playerInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("PlayerInfo", TableSchema.fromBean(PlayerInfoModel.class));
        return playerInfoModelDynamoDbTable.scan();
    }
}
