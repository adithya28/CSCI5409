package com.ad368540.ticTacToe.controller;

import com.ad368540.ticTacToe.model.*;
import com.ad368540.ticTacToe.repos.GameInfoRepository;
import com.ad368540.ticTacToe.repos.PlayerInfoRepository;
import com.ad368540.ticTacToe.repos.SessionInfoRepository;
import com.ad368540.ticTacToe.repos.TicTacToeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PlayerInfoRepository playerInfoRepository;
    @Autowired
    SessionInfoRepository sessionInfoRepository;
    @Autowired
    TicTacToeRepository ticTacToeRepository;
    @Autowired
    GameInfoRepository gameInfoRepository;
    @MessageMapping("/joinGame")
    @SendTo("/topic/updateGameState")
    public Object joinGame(@Payload JoinMessage message) {
        PlayerInfoModel playerInfoModel = new PlayerInfoModel();
        playerInfoModel.setPlayerName(message.getPlayerName());
        playerInfoModel.setDiscordName(message.getDiscordName());
        playerInfoModel.setGameID(message.getGameID());
        playerInfoRepository.save(playerInfoModel);
        boolean joined = ticTacToeRepository.joinGame(message.getPlayerName(),message.getGameID());
        if (!joined) {
            TicTacToeMessage errorMessage = new TicTacToeMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Error while joining game, Game doesn't exist, INVALID GAME ID");
            return errorMessage;
        }
        GameInfoModel currentGame = ticTacToeRepository.getGame(message.getGameID());
        TicTacToeMessage gameMessage = gameToMessage(currentGame);
        gameMessage.setType("joinedGame");
        return gameMessage;
    }
    @MessageMapping("/leaveGame")
    public void leaveGame(@Payload PlayerMessage message) {
        GameInfoModel game = ticTacToeRepository.leaveGame(message.getGameID());
        if (game != null) {
            TicTacToeMessage gameMessage = gameToMessage(game);
            gameMessage.setType("leaveGame");
            messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
        }
    }
    @MessageMapping("/makeMove")
    public void makeMove(@Payload TicTacToeMessage message) {
        String currentPlayer = message.getSender();
        String gameID = message.getGameID();
        int move = message.getMove();
        GameInfoModel game = ticTacToeRepository.getGame(gameID);
        if (game == null || game.getGameState().equals(GameState.PLAYER1_WON)||game.getGameState().equals(GameState.PLAYER2_WON)||game.getGameState().equals(GameState.GAME_INCOMPLETE)) {
            TicTacToeMessage errorMessage = new TicTacToeMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game not found or is already over.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameID, errorMessage);

        }
        else if (game.getGameState().equals(GameState.WAITING_FOR_PLAYER)) {
            TicTacToeMessage errorMessage = new TicTacToeMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game is waiting for another player to join.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameID, errorMessage);
        }
        else {
            if (game.getCurrentTurn().equals(currentPlayer)) {
                ticTacToeRepository.makeMove(gameID, currentPlayer, move);
                GameInfoModel currentGame= gameInfoRepository.findGameById(game.getGameID());
                TicTacToeMessage gameStateMessage = new TicTacToeMessage(currentGame);
                gameStateMessage.setType("makeMove");
                this.messagingTemplate.convertAndSend("/topic/game." + gameID, gameStateMessage);
                if (ticTacToeRepository.isGameOver(gameID)) {
                    TicTacToeMessage gameOverMessage = gameToMessage(currentGame);
                    gameOverMessage.setType("gameOver");
                    this.messagingTemplate.convertAndSend("/topic/game." + gameID, gameOverMessage);
                }
            }
        }
    }
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        if(headers.containsNativeHeader("login")) {
            String gameID = Objects.requireNonNull(headers.getNativeHeader("login")).get(0);
            // We store the session as we need to be idempotent in the disconnect event processing
            SessionInfoModel currentSession = new SessionInfoModel();
            currentSession.setSessionID(headers.getSessionId());
            currentSession.setGameID(gameID);
            sessionInfoRepository.save(currentSession);
        }
    }
   @EventListener
    public void SessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionID = headerAccessor.getSessionId();
        SessionInfoModel currentSession=sessionInfoRepository.getGameBySession(sessionID);
        if(sessionInfoRepository.getGameBySession(sessionID)!=null)
        {
        String gameID = currentSession.getGameID();
        GameInfoModel game = ticTacToeRepository.getGame(gameID);
        if (game != null) {
                    game.setGameState(GameState.GAME_INCOMPLETE);
                    TicTacToeMessage gameMessage = gameToMessage(game);
            gameInfoRepository.updateModel(game);
            gameMessage.setType("gameOver");
            messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
            }
        sessionInfoRepository.remove(currentSession);
        }
    }
    @MessageMapping("/createGame")
    public void createGame(@Payload PlayerMessage message) {
        GameInfoModel game = ticTacToeRepository.initializeGame(message.getGameID().trim());
        TicTacToeMessage gameMessage = gameToMessage(game);
        gameMessage.setType("createGame");
        messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
    }
    private TicTacToeMessage gameToMessage(GameInfoModel game) {
        return new TicTacToeMessage(game);
    }
}
