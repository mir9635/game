package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.entity.Player;

import java.util.List;

public interface PlayerService {
    List<Player> getPlayers(
        String name,
        String title,
        Race race,
        Profession profession,
        Long after,
        Long before,
        Boolean banned,
        Integer minExperience,
        Integer maxExperience,
        Integer minLevel,
        Integer maxLevel
    );

    List<Player> sortPlayers(List<Player> players, PlayerOrder order);

    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);

    boolean isPlayerValid(Player player);

    int computeLevel(int exp);

    int computeNextLevel(int exp, int lvl);

    Player savePlayer(Player player);

    Player getPlayer(Long id);

    Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalAccessException;

    void deletePlayer(Player player);
}
