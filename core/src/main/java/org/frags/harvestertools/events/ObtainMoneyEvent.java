package org.frags.harvestertools.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.enums.Tools;

public class ObtainMoneyEvent extends Event {


    private final Player player;

    private double amount;
    private final Tools tool;
    private final ItemStack itemStack;

    private static final HandlerList HANDLERS = new HandlerList();

    public ObtainMoneyEvent(Player player, double amount, Tools tool, ItemStack itemStack) {
        this.player = player;
        this.amount = amount;
        this.tool = tool;
        this.itemStack = itemStack;
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

    public Tools getTool() {
        return tool;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
