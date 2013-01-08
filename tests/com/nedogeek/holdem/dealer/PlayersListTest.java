package com.nedogeek.holdem.dealer;

import com.nedogeek.holdem.PlayerStatus;
import com.nedogeek.holdem.gamingStuff.Player;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Konstantin Demishev
 * Date: 22.11.12
 * Time: 1:55
 */
public class PlayersListTest {
    final Player firstPlayer = new Player("First player");
    final Player secondPlayer = new Player("Second player");

    private PlayersList playersList;
    private Dealer dealerMock;

    @Before
    public void setUp() throws Exception {
        resetPlayers();
        dealerMock = mock(Dealer.class);

        resetPlayerManager();
        setDefaultTwoPlayersGame();
    }

    private void resetPlayers() {
        firstPlayer.setStatus(PlayerStatus.NotMoved);
        secondPlayer.setStatus(PlayerStatus.NotMoved);
    }

    private void setDefaultTwoPlayersGame() {
        playersList.add(firstPlayer);
        playersList.add(secondPlayer);
    }

    @Test
    public void shouldFalseWhenDefaultDeskSecondPlayerFold() throws Exception {
        secondPlayer.setStatus(PlayerStatus.Fold);

        assertFalse(playersList.hasAvailableMovers());
    }

    @Test
    public void shouldFalseWhenDefaultDeskSecondPlayerLost() throws Exception {
        playersList.playerMoved(secondPlayer);
        secondPlayer.setStatus(PlayerStatus.Fold);

        assertFalse(playersList.hasAvailableMovers());
    }

    @Test
    public void shouldMoveWhen() throws Exception {
        firstPlayer.setStatus(PlayerStatus.Rise);
        secondPlayer.setStatus(PlayerStatus.Rise);
        playersList.playerMoved(firstPlayer);

        when(dealerMock.riseNeeded(secondPlayer)).thenReturn(true);

        assertEquals(secondPlayer, playersList.getMover());
    }

    @Test
    public void should0PlayersWhenNewPlayersManager() throws Exception {
        assertEquals(0, new PlayersList(dealerMock).size());
    }

    @Test
    public void should1PlayerWhenNewPlayerAddedToNewPlayersManager() throws Exception {
        resetPlayerManager();
        playersList.add(firstPlayer);

        assertEquals(1, playersList.size());
    }

    @Test
    public void should2PlayersWhen2NewPlayerAddedToNewPlayersManager() throws Exception {
        resetPlayerManager();
        playersList.add(firstPlayer);
        playersList.add(secondPlayer);

        assertEquals(2, playersList.size());
    }

    private void resetPlayerManager() {
        playersList = new PlayersList(dealerMock);
    }

    @Test
    public void should1PlayerWhenSameNewPlayerAddedToNewPlayersManagerTwice() throws Exception {
        resetPlayerManager();
        playersList.add(firstPlayer);
        playersList.add(firstPlayer);

        assertEquals(1, playersList.size());
    }

    @Test
    public void should2PlayersListWhenPlayersManagerGetPlayers() throws Exception {
        assertEquals(2, playersList.size());
    }

    @Test
    public void shouldPlayersListRegisteredInPlayerWhenPlayerAddedToList() throws Exception {
        final Player playerMock = mock(Player.class);

        playersList.add(playerMock);

        verify(playerMock).registerList(playersList);
    }

    @Test
    public void shouldMoverSecondPlayer0WhenMovedFirstPlayer() throws Exception {
        playersList.playerMoved(firstPlayer);

        assertEquals(secondPlayer, playersList.getMover());
    }


    @Test
    public void shouldMoverFirstPlayer0WhenMovedSecondPlayer() throws Exception {
        playersList.playerMoved(secondPlayer);

        assertEquals(firstPlayer, playersList.getMover());
    }

    @Test
    public void shouldSecondPlayerWhenSmallBlindPlayer() throws Exception {
        assertEquals(secondPlayer, playersList.smallBlindPlayer());
    }

    @Test
    public void shouldFirstPlayerWhenBigBlindPlayer() throws Exception {
        assertEquals(firstPlayer, playersList.bigBlindPlayer());
    }

    @Test
    public void shouldFirstPlayerWhenSmallBlindPlayerAfterSwitchingDealer() throws Exception {
        playersList.changeDealer();

        assertEquals(firstPlayer, playersList.smallBlindPlayer());
    }

    @Test
    public void shouldSecondPlayerWhenBigBlindPlayerAfterSwitchingDealer() throws Exception {
        playersList.changeDealer();

        assertEquals(secondPlayer, playersList.bigBlindPlayer());
    }

    @Test
    public void shouldThirdPlayerWhenAddThirdPlayerAndGetBigBlindPlayer() throws Exception {
        Player thirdPlayer = mock(Player.class);
        playersList.add(thirdPlayer);

        assertEquals(thirdPlayer, playersList.bigBlindPlayer());
    }
}