package com.nedogeek.holdem.dealer;

/*-
 * #%L
 * Holdem dojo project is a server-side java application for playing holdem pocker in DOJO style.
 * %%
 * Copyright (C) 2016 Holdemdojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.nedogeek.holdem.GameCenter;
import com.nedogeek.holdem.gameEvents.AddPlayerEvent;
import com.nedogeek.holdem.gameEvents.Event;
import com.nedogeek.holdem.gameEvents.GameEndedEvent;
import com.nedogeek.holdem.gameEvents.RemovePlayerEvent;
import com.nedogeek.holdem.gamingStuff.Card;
import com.nedogeek.holdem.gamingStuff.Player;
import com.nedogeek.holdem.gamingStuff.PlayersList;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Konstantin Demishev
 * Date: 08.02.13
 * Time: 22:10
 */
public class EventManager {
    private final String PUBLIC = "public";
    private final String gameId;
    private final GameCenter gameCenter;

    private PlayersList playersList;
    private Dealer dealer;
    private Event event;

    public EventManager(String gameId, GameCenter gameCenter) {
        this.gameId = gameId;
        this.gameCenter = gameCenter;
    }

    private void notifyConnections() {
        gameCenter.notifyViewers(gameId, gameToJSON(PUBLIC));

        for (String playerName : playersList.getPlayerNames()) {
            String message = gameToJSON(playerName);
            gameCenter.notifyPlayer(playerName, message);
        }
    }

    public void addEvent(Event event) {
        processEvents(event);

        this.event = event;

        notifyConnections();
    }

    private void processEvents(Event event) {
        if (event instanceof AddPlayerEvent || event instanceof RemovePlayerEvent) {
            dealer.calculateGameStatus();
        }
    }

    String gameToJSON(String connectionName) {
        String playersJSON = generatePlayersJSON(connectionName);

        String gameStatus = dealer.getGameStatus().toString();
        String gameRound = dealer.getGameRound().toString();
        int pot = playersList.getPot();
        String deskCards = generateCardsJSON();
        String event = this.event.toJSON();

        String dealerName = playersList.getDealerName();
        String moverName = playersList.getMoverName();

        String message = "{";
        message += "\"gameRound\":\"" + gameRound + "\"";
        message += "," + "\"dealer\":\"" + dealerName + "\"";
        message += "," + "\"mover\":\"" + moverName + "\"";
        message += "," + "\"event\":" + event;
        message += "," + "\"players\":" + playersJSON;
        if (!connectionName.equals(PUBLIC)) {
            message += "," + "\"combination\":\"" + playersList.getPlayerCardCombination(connectionName) + "\"";
        }
        message += "," + "\"gameStatus\":\"" + gameStatus + "\"";

        message += "," + "\"deskCards\":" + deskCards;
        message += "," + "\"deskPot\":" + pot;


        message += "}";
        return message;
    }

    private String generateCardsJSON() {
        String cardsJSON = "[";

        if (dealer.getDeskCards().length > 0) {
            for (Card card : dealer.getDeskCards()) {
                cardsJSON += card.toJSON() + ",";
            }
            cardsJSON = cardsJSON.substring(0, cardsJSON.length() - 1);
        }
        cardsJSON += "]";

        return cardsJSON;
    }

    private String generatePlayersJSON(String connectionName) {
        List<String> playersWithCards = new ArrayList<>();

        if (!connectionName.equals(PUBLIC)) {
            playersWithCards.add(connectionName);
        }

        if (event instanceof GameEndedEvent) {
            List<Player> winners = ((GameEndedEvent) event).getWinners();
            for (Player winner : winners) {
                String winnerName = winner.getName();
                if (!connectionName.equals(winnerName)) {
                    playersWithCards.add(winnerName);
                }
            }
        }

        String[] playerNamesArray = new String[playersWithCards.size()];
        for (int i = 0; i < playersWithCards.size(); i++) {
            playerNamesArray[i] = playersWithCards.get(i);
        }

        return playersList.generatePlayersJSON(playerNamesArray);
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    public void setPlayersList(PlayersList playersList) {
        this.playersList = playersList;
    }
}
