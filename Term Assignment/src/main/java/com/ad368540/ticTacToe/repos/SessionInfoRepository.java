package com.ad368540.ticTacToe.repos;

import com.ad368540.ticTacToe.model.GameInfoModel;
import com.ad368540.ticTacToe.model.SessionInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository

public class SessionInfoRepository {
    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    public SessionInfoModel getGameBySession(String sessionID)
    {
        DynamoDbTable<SessionInfoModel> sessionInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("SessionInfo", TableSchema.fromBean(SessionInfoModel.class));
        Key key = Key.builder()
                .partitionValue(sessionID)
                .build();
        SessionInfoModel currentGame = sessionInfoModelDynamoDbTable.getItem(key);
        if(currentGame!=null)
            return sessionInfoModelDynamoDbTable.getItem(key);
        return null;
    }
    public void save(SessionInfoModel sessionInfoModel)
    {
        DynamoDbTable<SessionInfoModel> sessionInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("SessionInfo", TableSchema.fromBean(SessionInfoModel.class));
        sessionInfoModelDynamoDbTable.putItem(sessionInfoModel);
    }
    public void update(SessionInfoModel sessionInfoModel)
    {
        DynamoDbTable<SessionInfoModel> sessionInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("SessionInfo", TableSchema.fromBean(SessionInfoModel.class));
        sessionInfoModelDynamoDbTable.updateItem(sessionInfoModel);
    }
    public void remove(SessionInfoModel sessionInfoModel)
    {
        DynamoDbTable<SessionInfoModel> sessionInfoModelDynamoDbTable =
                dynamoDbEnhancedClient.table("SessionInfo", TableSchema.fromBean(SessionInfoModel.class));
        sessionInfoModelDynamoDbTable.deleteItem(sessionInfoModel);
    }
}
