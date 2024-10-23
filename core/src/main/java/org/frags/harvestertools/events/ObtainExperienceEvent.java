package org.frags.harvestertools.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ObtainExperienceEvent extends Event {

    private final Player player;

    private double amount;

    private final ItemStack itemStack;

    private static final HandlerList HANDLERS = new HandlerList();

    public ObtainExperienceEvent(Player player, double amount, ItemStack itemStack) {
        this.player = player;
        this.amount = amount;
        this.itemStack = itemStack;
    }

    public Player getPlayer() {
        return player;
    }

    public double getExperience() {
        return amount;
    }

    public void setExperience(double amount) {
        this.amount = amount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ItemStack getTool() {
        return itemStack;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
