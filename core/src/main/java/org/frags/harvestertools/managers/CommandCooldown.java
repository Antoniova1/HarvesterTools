package org.frags.harvestertools.managers;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandCooldown {

    private Player player;

    public CommandCooldown(Player player, Duration duration) {
        this.player = player;
        setCooldown(duration);
    }


    private final Map<Player, Instant> map = new HashMap<>();

    public void setCooldown(Duration duration) {
        map.put(player, Instant.now().plus(duration));
    }

    public boolean hasCooldown() {
        Instant cooldown = map.get(player);
        return cooldown != null && Instant.now().isBefore(cooldown);
    }

    public Instant removeCooldown() {
        return map.remove(player);
    }

    public Duration getRemainingCooldown() {
        Instant cooldown = map.get(player);
        Instant now = Instant.now();
        if (cooldown != null && now.isBefore(cooldown)) {
            return Duration.between(now, cooldown);
        } else {
            return Duration.ZERO;
        }
    }
}
