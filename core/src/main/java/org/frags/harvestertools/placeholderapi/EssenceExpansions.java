package org.frags.harvestertools.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.managers.LeaderBoardManager;
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
        //%harvestertools_top_essence_(balance)(player)_1%
        if (params.contains("_")) {
            String[] split = params.split("_");

            if (split[0].equalsIgnoreCase("top")) {

                if (split[1].equalsIgnoreCase("essence")) {
                    int top = Integer.parseInt(split[3]) - 1;
                    LeaderBoardManager manager = plugin.getEssenceManager().getPlayerEssenceTop(top);
                    if (manager != null) {
                        if (split[2].equalsIgnoreCase("balance"))
                            return manager.getFormattedBalance();
                        if (split[2].equalsIgnoreCase("player"))
                            return manager.getPlayerName();
                    }
                }
            }
        }

        if (params.equalsIgnoreCase("essence")) {
            return String.valueOf(plugin.getEssenceManager().getEssence(player));
        } else if (params.equalsIgnoreCase("essence_formatted")) {
            return plugin.getEssenceManager().getFormattedEssence(player);
        }


        return null;
    }
}
