package com.ad368540.ticTacToe.repos;

import com.ad368540.ticTacToe.model.GameInfoModel;
import com.ad368540.ticTacToe.model.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;


@Repository
public class TicTacToeRepository {
    @Autowired
    GameInfoRepository gameInfoRepository;
    public synchronized GameInfoModel initializeGame(String gameID) {
        GameInfoModel currentGameInfo = new GameInfoModel();
        currentGameInfo.setGameID(gameID);
        currentGameInfo.setHostPlayer("");
        String[][] gameBoard;
        gameBoard = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameBoard[i][j] = " ";
             }
        }
        currentGameInfo.setGameState(GameState.WAITING_FOR_PLAYER);
        currentGameInfo.setCurrentTurn("");
        currentGameInfo.saveGameBoard(gameBoard);
        gameInfoRepository.save(currentGameInfo);
        return currentGameInfo;
    }
    public synchronized boolean joinGame(String player, String gameID) {
        if(gameInfoRepository.findGameById(gameID)==null)
            return false;
        GameInfoModel currentGame = gameInfoRepository.findGameById(gameID);
        if (currentGame.getHostPlayer().equals(""))
            currentGame.setHostPlayer(player);
        else if (currentGame.getGuestPlayer()==null)
            currentGame.setGuestPlayer(player);
        currentGame.setGameState(GameState.PLAYER1_TURN);
        currentGame.setCurrentTurn(currentGame.getHostPlayer());
        gameInfoRepository.updateModel(currentGame);
        return true;
        }
    public synchronized GameInfoModel leaveGame(String gameID) {
        if (gameID != null) {
            GameInfoModel game = gameInfoRepository.findGameById(gameID);
            game.setGameState(GameState.GAME_INCOMPLETE);
            return game;
            }
            return null;
        }
    public GameInfoModel getGame(String gameID) {
        return gameInfoRepository.findGameById(gameID);
    }

    public void makeMove(String gameID,String player, int move) {
        GameInfoModel currentGame = gameInfoRepository.findGameById(gameID);
        int row = move / 3;
        int col = move % 3;
        String[][] gameBoard =currentGame.fetchGameBoard();
        if (gameBoard[row][col].equals(" ")) {
            gameBoard[row][col] = Objects.equals(player, currentGame.getHostPlayer()) ? "X" : "O";
            if(player.equals(currentGame.getHostPlayer()))
                currentGame.setCurrentTurn(currentGame.getGuestPlayer());
            else
                currentGame.setCurrentTurn(currentGame.getHostPlayer());
            currentGame.saveGameBoard(gameBoard);
            gameInfoRepository.updateModel(currentGame);

            updateGameState(gameID);
        }
    }
    public boolean isGameOver(String gameID) {
       return isBoardFull(gameID);
    }
    private boolean isBoardFull(String gameID) {
        GameInfoModel currentGame = gameInfoRepository.findGameById(gameID);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Objects.equals(currentGame.fetchGameBoard()[i][j], " ")) {
                    return false;
                }
            }
        }
        return true;
    }
    private void updateGameState(String gameID) {
        GameInfoModel game = gameInfoRepository.findGameById(gameID);
        GameInfoModel currentGame=checkWinner(gameID);
        if(game.getWinner()==null) {
            if (currentGame.getWinner() != null) {
               if (currentGame.getWinner().equals(currentGame.getHostPlayer()))
                    currentGame.setGameState(GameState.PLAYER1_WON);
                else
                    currentGame.setGameState(GameState.PLAYER2_WON);
                gameInfoRepository.updateModel(currentGame);
                return;
            }
        }
        if (isBoardFull(gameID)) {
                game.setGameState(GameState.TIE);
                gameInfoRepository.updateModel(game);
            } else {
                if (game.getCurrentTurn().equals(game.getHostPlayer()))
                    game.setGameState(GameState.PLAYER1_TURN);
                else
                    game.setGameState(GameState.PLAYER2_TURN);
            gameInfoRepository.updateModel(game);
            }

    }
    private GameInfoModel checkWinner(String gameID) {
        GameInfoModel currentGame = gameInfoRepository.findGameById(gameID);
        for (int i = 0; i < 3; i++) {
            if (Objects.equals(currentGame.fetchGameBoard()[i][0], currentGame.fetchGameBoard()[i][1]) && Objects.equals(currentGame.fetchGameBoard()[i][0], currentGame.fetchGameBoard()[i][2])) {
                if (!Objects.equals(currentGame.fetchGameBoard()[i][0], " ")) {
                    currentGame.setWinner(Objects.equals(currentGame.fetchGameBoard()[i][0], "X") ? currentGame.getHostPlayer() : currentGame.getGuestPlayer());
                    return currentGame;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            if (Objects.equals(currentGame.fetchGameBoard()[0][i], currentGame.fetchGameBoard()[1][i]) && Objects.equals(currentGame.fetchGameBoard()[0][i], currentGame.fetchGameBoard()[2][i])) {
                if (!Objects.equals(currentGame.fetchGameBoard()[0][i], " ")) {
                    currentGame.setWinner(Objects.equals(currentGame.fetchGameBoard()[0][i], "X") ? currentGame.getHostPlayer() : currentGame.getGuestPlayer());
                    return currentGame;
                }
            }
        }
        if (Objects.equals(currentGame.fetchGameBoard()[0][0], currentGame.fetchGameBoard()[1][1]) && Objects.equals(currentGame.fetchGameBoard()[0][0], currentGame.fetchGameBoard()[2][2])) {
            if (!Objects.equals(currentGame.fetchGameBoard()[0][0], " ")) {
                currentGame.setWinner(Objects.equals(currentGame.fetchGameBoard()[0][0], "X") ? currentGame.getHostPlayer() : currentGame.getGuestPlayer());

            }
        }
        return currentGame;
    }
}
