package com.ad368540.ticTacToe.model;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class GameInfoModel
{
    @Getter(onMethod_={@DynamoDbPartitionKey})
    private String gameID;
    private  String hostPlayer;
    private String guestPlayer;
    private GameState gameState;
    private String currentTurn;
    private String winner;
    private String gameBoard;
    public String[][] fetchGameBoard()
    {
        String[][] currentGameBoard = new String[3][3];
        for (int i=0;i<9;i++)
        {
            int rowIndex = i/3;
            int columnIndex = i%3;
            if(this.gameBoard.charAt(i)=='*')
                currentGameBoard[rowIndex][columnIndex]= String.valueOf(' ');
            else
                currentGameBoard[rowIndex][columnIndex]= String.valueOf(gameBoard.charAt(i));
        }
        return currentGameBoard;
    }
    public void saveGameBoard(String[][] currentGameBoard)
    {
        if(currentGameBoard==null)
            return;
        StringBuilder temp = new StringBuilder();
        for (int i=0;i<9;i++)
        {
            int rowIndex = i/3;
            int columnIndex = i%3;
            if(currentGameBoard[rowIndex][columnIndex].equals(" "))
                temp.replace(i,i,"*");
            else
                temp.replace(i,i,currentGameBoard[rowIndex][columnIndex]);
        }
        this.gameBoard= String.valueOf(temp);
    }
}
