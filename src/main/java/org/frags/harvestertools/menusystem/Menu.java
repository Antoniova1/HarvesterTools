package org.frags.harvestertools.menusystem;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;

public abstract class Menu implements InventoryHolder {

    protected final HarvesterTools plugin;

    protected PlayerMenuUtility playerMenuUtility;

    protected Inventory inventory;

    public Menu(HarvesterTools plugin, PlayerMenuUtility playerMenuUtility) {
        this.plugin = plugin;
        this.playerMenuUtility = playerMenuUtility;
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        this.setMenuItems();

        playerMenuUtility.getOwner().openInventory(inventory);
    }

    protected String miniMessageParser(String string) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextComponent text = (TextComponent) miniMessage.deserialize(string);
        return LegacyComponentSerializer.legacySection().serialize(text);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setFillerGlass() {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                try {
                    ItemStack FILLER_GLASS = new ItemStack(Material.getMaterial(plugin.menuFile.getConfig().getString("filler-item")));
                    inventory.setItem(i, FILLER_GLASS);
                } catch (NullPointerException e) {
                    Bukkit.getLogger().warning("You used an invalid material in filler-item - menu.yml, please change it.");
                    break;
                }
            }
        }
    }
}
