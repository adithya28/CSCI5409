package com.ad368540.ticTacToe.controller;

import com.ad368540.ticTacToe.model.GameInfoModel;
import com.ad368540.ticTacToe.model.GameState;
import com.ad368540.ticTacToe.repos.GameInfoRepository;
import com.ad368540.ticTacToe.repos.TicTacToeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.ArrayList;
import java.util.List;


@Controller
public class GameController {
    @Autowired
    TicTacToeRepository ticTacToeRepository;
    @Autowired
    GameInfoRepository gameInfoRepository;

    @GetMapping({"/game/{id}"})
    public ModelAndView getIndex(@PathVariable("id") String gameID) {
        ModelAndView modelAndView = new ModelAndView("gameBoard");
        String[][] board = new String[3][3];
        board = ticTacToeRepository.getGame(gameID).fetchGameBoard();
        modelAndView.addObject("board", board);
        return modelAndView;
    }
    @GetMapping({"/createGame"})
    public ModelAndView getCreateGame() {
        PageIterable<GameInfoModel> allGames = gameInfoRepository.getAllGames();

        List<GameInfoModel> games = new ArrayList<>();
        List<GameInfoModel> gamesList = new ArrayList<>();

        allGames.items().stream().forEach(games::add);
        for(GameInfoModel item:games)
        {
            if(item.getGameState().equals(GameState.WAITING_FOR_PLAYER))
            {
                gamesList.add(item);
            }
        }
        ModelAndView modelAndView = new ModelAndView("createGame");
        modelAndView.addObject("available_games",gamesList);
        return modelAndView;
    }
}
