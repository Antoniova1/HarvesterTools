package org.frags.harvestertools.nms_1_20_6;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.frags.harvestertools.HarvesterTools;
import org.jetbrains.annotations.NotNull;

public record ReplenishCrop(Block block, BlockPos blockPos, int x, int y, int z, LevelChunk levelChunk, LevelChunkSection section, BlockState grownState, long time, HarvesterTools plugin) implements Comparable<ReplenishCrop> {

    public void unloadChunk() {
        Chunk chunk = block.getChunk();
        if (!chunk.getPluginChunkTickets().isEmpty())
            chunk.removePluginChunkTicket(plugin);
    }

    public void restoreCrop() {

        World world = block.getWorld();
        Chunk chunk = block.getChunk();

        if (!chunk.isLoaded()) {
            world.loadChunk(chunk);
            chunk.setForceLoaded(true);
            chunk.load();
        }

        levelChunk.setLoaded(true);

        section.setBlockState(x, y, z, grownState);

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockPos, grownState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase(block.getWorld().getName()))
                continue;
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();
            serverPlayer.connection.send(packet);
        }

    }

    @Override
    public int compareTo(@NotNull ReplenishCrop o) {
        return Long.compare(time, o.time);
    }
}
