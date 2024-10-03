package org.frags.harvestertools.managers;

import org.frags.harvestertools.utils.Utils;

import java.math.BigDecimal;

public class LeaderBoardManager {

    private final String playerName;
    private final double balance;
    private final String formattedBalance;

    public LeaderBoardManager(String playerName, double balance) {
        this.playerName = playerName;
        this.balance = balance;
        this.formattedBalance = Utils.formatNumber(BigDecimal.valueOf(balance));
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getBalance() {
        return balance;
    }

    public String getFormattedBalance() {
        return formattedBalance;
    }
}
