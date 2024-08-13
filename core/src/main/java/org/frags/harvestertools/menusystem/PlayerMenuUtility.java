package org.frags.harvestertools.menusystem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.enchants.CustomEnchant;
import org.frags.harvestertools.enums.Tools;

public class PlayerMenuUtility {

    private Player owner;
    private Tools tool;
    private ItemStack item;
    private CustomEnchant enchant;

    public PlayerMenuUtility(Player player, Tools tool, ItemStack item) {
        this.owner = player;
        this.tool = tool;
        this.item = item;
    }

    public PlayerMenuUtility(Player player) {
        this.owner = player;
    }

    public Player getOwner() {
        return owner;
    }

    public Tools getTool() {
        return tool;
    }

    public ItemStack getItem() {
        return item;
    }

    public CustomEnchant getEnchant() {
        return enchant;
    }

    public void setEnchant(CustomEnchant enchant) {
        this.enchant = enchant;
    }
}
