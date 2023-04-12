package com.ad368540.ticTacToe.repos;
import com.ad368540.ticTacToe.model.GameInfoModel;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

@Repository

public class GameInfoRepository {
    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public void save(GameInfoModel gameInfoModel)
    {
        DynamoDbTable<GameInfoModel> gameInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("GameInfo", TableSchema.fromBean(GameInfoModel.class));
        gameInfoModelDynamoDbTable.putItem(gameInfoModel);
    }
    public void updateModel(GameInfoModel gameInfoModel)
    {
        DynamoDbTable<GameInfoModel> gameInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("GameInfo", TableSchema.fromBean(GameInfoModel.class));
        gameInfoModelDynamoDbTable.updateItem(gameInfoModel);

    }
    public GameInfoModel findGameById(String gameID)
    {
        DynamoDbTable<GameInfoModel> gameInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("GameInfo", TableSchema.fromBean(GameInfoModel.class));
        Key key = Key.builder()
                .partitionValue(gameID)
                .build();
        GameInfoModel currentGame = gameInfoModelDynamoDbTable.getItem(key);
        if(currentGame!=null)
            return gameInfoModelDynamoDbTable.getItem(key);
        return null;
    }
    public PageIterable<GameInfoModel> getAllGames()
    {
        DynamoDbTable<GameInfoModel> gameInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("GameInfo", TableSchema.fromBean(GameInfoModel.class));
        return gameInfoModelDynamoDbTable.scan();
    }
}
