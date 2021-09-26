package com.game.controller;


import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {


    private PlayerService playerService;

    public PlayerController(){}

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.GET)
    public List<Player> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
            ){
        final List<Player> players = playerService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel);
        final List<Player> sortedPlayers = playerService.sortPlayers(players, order);

        return playerService.getPage(sortedPlayers, pageNumber, pageSize);
    }

    @RequestMapping(path = "/rest/players/count", method = RequestMethod.GET)
    public Integer getPlayersCount(
         @RequestParam(value = "name", required = false) String name,
         @RequestParam(value = "title", required = false) String title,
         @RequestParam(value = "race", required = false) Race race,
         @RequestParam(value = "profession", required = false) Profession profession,
         @RequestParam(value = "after", required = false) Long after,
         @RequestParam(value = "before", required = false) Long before,
         @RequestParam(value = "banned", required = false) Boolean banned,
         @RequestParam(value = "minExperience", required = false) Integer minExperience,
         @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
         @RequestParam(value = "minLevel", required = false) Integer minLevel,
         @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ){
        return playerService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> createShip(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (player.getBanned() == null) player.setBanned(false);
        final int level = playerService.computeLevel(player.getExperience());
        player.setLevel(level);

        final int nextLevel = playerService.computeNextLevel(level, player.getExperience());
        player.setUntilNextLevel(nextLevel);

        final Player savedPlayer = playerService.savePlayer(player);

        return  new ResponseEntity<>(savedPlayer, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> getPlayer(@PathVariable(value = "id") String pathId) {
        final Long id = convertIdToLong(pathId);
        if (id == null || id <=0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final Player player = playerService.getPlayer(id);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(path = "rest/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable(value = "id") String pathId, @RequestBody Player updatePlayer) {
        if (!checkPlayer(updatePlayer, true)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Player player;
        if (getPlayer(pathId).getStatusCode().equals(HttpStatus.OK)) {
            player = getPlayer(pathId).getBody();
        } else {
            return new ResponseEntity<>(getPlayer(pathId).getStatusCode());
        }
        try {
            player = playerService.updatePlayer(player, updatePlayer);
            return new ResponseEntity<>(player, HttpStatus.OK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.DELETE)
    private ResponseEntity<Player> deletePlayer(@PathVariable(value = "id") String pathId) {
        final ResponseEntity<Player> entity = getPlayer(pathId);
        final Player savedPlayer = entity.getBody();
        if (savedPlayer == null) {
            return entity;
        }
        playerService.deletePlayer(savedPlayer);
        return new ResponseEntity<>(HttpStatus.OK);
    }



    private Long convertIdToLong(String pathId) {
        if (pathId == null) {
            return null;
        } else try {
            return Long.parseLong(pathId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean checkPlayer(Player player, boolean isUpdating) {
        if (!isUpdating) {
            if (player.getName() == null
                    || player.getTitle() == null
                    || player.getRace() == null
                    || player.getProfession() == null
                    || player.getBirthday() == null
                    || player.getExperience() == null) {
                return false;
            }
        }
        if ((player.getName() != null) && player.getName().length() > 12
                || (player.getName() != null) && player.getName().trim().length() == 0
                || (player.getTitle() != null) && player.getTitle().length() > 30
                || (player.getExperience() != null) && (player.getExperience() < 0 || player.getExperience() > 10000000)
                || (player.getBirthday() != null) && player.getBirthday().getTime() < 946674000000L
                || (player.getBirthday() != null) && player.getBirthday().getTime() > 32535205199999L) {
            return false;
        }
        if (player.getBanned() == null) {
            player.setBanned(false);
        }
        return true;
    }



}
