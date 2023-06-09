package com.ad368540.ticTacToe.controller;

import com.ad368540.ticTacToe.model.*;
import com.ad368540.ticTacToe.repos.GameInfoRepository;
import com.ad368540.ticTacToe.repos.PlayerInfoRepository;
import com.ad368540.ticTacToe.repos.SessionInfoRepository;
import com.ad368540.ticTacToe.repos.TicTacToeRepository;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
    @Value("${amazon.apigateway.endpoint}")
    private String apiEndpoint;
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
            GameMessage errorMessage = new GameMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Error while joining game, Game doesn't exist, INVALID GAME ID");
            return errorMessage;
        }
        GameInfoModel currentGame = ticTacToeRepository.getGame(message.getGameID());
        GameMessage gameMessage = gameToMessage(currentGame);
        gameMessage.setType("joinedGame");
        return gameMessage;
    }
    @MessageMapping("/leaveGame")
    public void leaveGame(@Payload PlayerMessage message) {
        GameInfoModel game = ticTacToeRepository.leaveGame(message.getGameID());
        if (game != null) {
            GameMessage gameMessage = gameToMessage(game);
            gameMessage.setType("leaveGame");
            messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
        }
    }
    @MessageMapping("/makeMove")
    public void makeMove(@Payload GameMessage message) {
        String currentPlayer = message.getSender();
        String gameID = message.getGameID();
        int move = message.getMove();
        GameInfoModel game = ticTacToeRepository.getGame(gameID);
        if (game == null || game.getGameState().equals(GameState.PLAYER1_WON)||game.getGameState().equals(GameState.PLAYER2_WON)||game.getGameState().equals(GameState.GAME_INCOMPLETE)) {
            GameMessage errorMessage = new GameMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game not found or is already over.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameID, errorMessage);
        }
        else if (game.getGameState().equals(GameState.WAITING_FOR_PLAYER)) {
            GameMessage errorMessage = new GameMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game is waiting for another player to join.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameID, errorMessage);
        }
        else {
            if (game.getCurrentTurn().equals(currentPlayer)) {
                ticTacToeRepository.makeMove(gameID, currentPlayer, move);
                GameInfoModel currentGame= gameInfoRepository.findGameById(game.getGameID());
                GameMessage gameStateMessage = new GameMessage(currentGame);
                gameStateMessage.setType("makeMove");
                this.messagingTemplate.convertAndSend("/topic/game." + gameID, gameStateMessage);
                if (ticTacToeRepository.isGameOver(gameID)) {
                    GameMessage gameOverMessage = gameToMessage(currentGame);
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
                    GameMessage gameMessage = gameToMessage(game);
            gameInfoRepository.updateModel(game);
            gameMessage.setType("gameOver");
            messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
            }
        sessionInfoRepository.remove(currentSession);
        }
    }
    @MessageMapping("/createGame")
    public void createGame(@Payload PlayerMessage message) throws MessagingException {
        GameInfoModel game = ticTacToeRepository.initializeGame(message.getGameID().trim());
        GameMessage gameMessage = gameToMessage(game);
        gameMessage.setType("createGame");
        messagingTemplate.convertAndSend("/topic/game." + game.getGameID(), gameMessage);
    }
    @MessageMapping("/invokeLambda")
    public void invokeLambda(@Payload PlayerMessage message) throws IOException, InterruptedException {
        String targetPlayer=null;
        String targetDiscordName=null;
        HttpClient client = HttpClient.newHttpClient();
        if(gameInfoRepository.findGameById(message.getGameID()).getHostPlayer().equals(message.getPlayerName()))
            targetPlayer=gameInfoRepository.findGameById(message.getGameID()).getGuestPlayer();
        else
            targetPlayer=gameInfoRepository.findGameById(message.getGameID()).getHostPlayer();
        targetDiscordName=playerInfoRepository.findPlayerInfoByName(targetPlayer).getDiscordName();
        String lambdaPayload = "{\"discordName\":\""+targetDiscordName+"\",\"messageData\":\""+message.getContent()+
                "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(lambdaPayload))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    private GameMessage gameToMessage(GameInfoModel game) {
        return new GameMessage(game);
    }
}
