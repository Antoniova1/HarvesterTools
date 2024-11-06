package org.frags.harvestertools.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.enums.Tools;

public class ObtainExperienceEvent extends Event {

    private final Player player;

    private double amount;

    private final ItemStack itemStack;
    private final Tools tool;

    private static final HandlerList HANDLERS = new HandlerList();

    public ObtainExperienceEvent(Player player, double amount, Tools tool, ItemStack itemStack) {
        this.player = player;
        this.amount = amount;
        this.itemStack = itemStack;
        this.tool = tool;
    }

    public Player getPlayer() {
        return player;
    }

    public double getExperience() {
        return amount;
    }

    public Tools getTool() {
        return tool;
    }

    public void setExperience(double amount) {
        this.amount = amount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
