package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    private PlayerRepository playerRepository;

    public PlayerServiceImpl(){}

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPlayers(
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
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> list = new ArrayList<>();

        playerRepository.findAll().forEach((player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            list.add(player);
        }));
        return list;
    }

    @Override
    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) ->{
                switch (order) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    default: return 0;
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ?  3 : pageSize;
        final  int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return  players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return dataParams(player) &&
                isParamsDateValue(player.getBirthday()) &&
                isTextValue(player);
    }


    private boolean dataParams(Player player) {
        return !player.getName().isEmpty() && !player.getTitle().isEmpty() && !Objects.isNull(player.getRace()) && !Objects.isNull(player.getProfession())
                && player.getExperience() >= 0;
    }

    private boolean isParamsDateValue(Date date) {
        final Date startDate = getDateForYear(2000);
        final Date endDate = getDateForYear(3000);
        return date !=null && date.after(startDate) && date.before(endDate);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private Boolean isTextValue(Player player){
        return  isNameSizeValue(player.getName()) && isTitleSizeValue(player.getTitle());
    }

    private Boolean isNameSizeValue(String text) {
        return text.length() <= 12;
    }
    private Boolean isTitleSizeValue(String text) {
        return text.length() <= 30;
    }

    @Override
    public int computeLevel(int exp) {
        return  (int)Math.floor((Math.sqrt(2500 + 200 * exp)-50)/100);
    }

    @Override
    public int computeNextLevel(int exp, int lvl) {
        return 50* (lvl + 1) * (lvl + 2) - exp;
    }

    @Override
    public Player savePlayer(Player player) {
        player.setLevel(computeLevel(player.getExperience()));
        player.setUntilNextLevel(computeNextLevel(player.getExperience(), player.getLevel()));
        return playerRepository.save(player);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalAccessException {
        boolean shouldChange = false;

        final String name = newPlayer.getName();
        if (name !=null) {
            if (isNameSizeValue(name)) {
                oldPlayer.setName(name);
            } else {
                throw  new IllegalAccessException();
            }
        }

        final String title = newPlayer.getTitle();
        if (title !=null) {
            if (isTitleSizeValue(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalAccessException();
            }
        }

        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }
        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }

        final Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
           if (isParamsDateValue(birthday)) {
               oldPlayer.setBirthday(birthday);
           } else {
               throw new IllegalAccessException();
           }
        }

        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
        }

        final Integer experience = newPlayer.getExperience();
        if (experience != null) {
            oldPlayer.setExperience(experience);
            shouldChange = true;
        }

        if (shouldChange) {
            Integer level = computeLevel(oldPlayer.getExperience());
            oldPlayer.setLevel(level);

            Integer untilNextLevel = computeNextLevel(oldPlayer.getExperience(), oldPlayer.getLevel());
            oldPlayer.setUntilNextLevel(untilNextLevel);
        }
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void deletePlayer(Player player) {
       playerRepository.delete(player);
    }
}
