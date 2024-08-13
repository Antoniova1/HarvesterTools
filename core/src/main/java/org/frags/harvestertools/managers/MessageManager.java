package org.frags.harvestertools.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static org.frags.harvestertools.HarvesterTools.getInstance;

public class MessageManager {

    private static BukkitAudiences adventure;

    public static void setAdventure(BukkitAudiences adventureInstance) {
        adventure = adventureInstance;
    }

    public static String miniMessageParse(Player player, String input) throws NullPointerException {
        String message = getInstance().messages.getConfig().getString(input);
        if (getInstance().getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            return miniStringParse(parsePlaceholderAPI(player, input));
        return miniStringParse(message);
    }

    public static String miniStringParse(String input) throws NullPointerException {
        if (input == null)
            throw new NullPointerException();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextComponent text = (TextComponent) miniMessage.deserialize(input);
        return LegacyComponentSerializer.legacySection().serialize(text);
    }

    public static void miniMessageSender(Player player, String message) {
        if (adventure == null) {
            throw new IllegalStateException("BukkitAudiences is not initialized.");
        }
        MiniMessage miniMessage = MiniMessage.miniMessage();
        TextComponent text;
        if (getInstance().getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = (TextComponent) miniMessage.deserialize(parsePlaceholderAPI(player, message));
        } else {
            text = (TextComponent) miniMessage.deserialize(message);
        }
        adventure.player(player).sendMessage(text);
    }

    public static String parsePlaceholderAPI(Player player,String input) {
        String text = input;
        text = PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }
}
