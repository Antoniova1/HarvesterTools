package org.frags.harvestertools.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.customevents.Rewards;
import org.frags.harvestertools.enums.Tools;
import org.frags.harvestertools.toolsmanagers.ToolManager;

import java.util.HashMap;
import java.util.List;

public class ObtainEssenceEvent extends Event {

    private final Player player;

    private double amount;
    private final Tools tool;
    private final ItemStack itemStack;
    private final ToolManager toolManager;

    private static final HandlerList HANDLERS = new HandlerList();

    public ObtainEssenceEvent(Player player, double amount, Tools tool, ItemStack itemStack, ToolManager toolManager) {
        this.player = player;
        this.amount = amount;
        this.itemStack = itemStack;
        this.tool = tool;
        this.toolManager = toolManager;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public ToolManager getToolManager() {
        return toolManager;
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
