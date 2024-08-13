package org.frags.harvestertools.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EssenceExpansions extends PlaceholderExpansion {

    private final HarvesterTools plugin;

    public EssenceExpansions(HarvesterTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "harvestertools";
    }

    @Override
    public @NotNull String getAuthor() {
        return "frags";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (player == null) return null;

        if (params.equalsIgnoreCase("essence")) {
            return String.valueOf(plugin.getEssenceManager().getEssence(player));
        } else if (params.equalsIgnoreCase("essence_formatted")) {
            return plugin.getEssenceManager().getFormattedEssence(player);
        }


        return null;
    }
}
