package org.frags.harvestertools.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ObtainMoneyEvent extends Event {


    private final Player player;

    private double amount;

    private static final HandlerList HANDLERS = new HandlerList();

    public ObtainMoneyEvent(Player player, double amount) {
        this.player = player;
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
