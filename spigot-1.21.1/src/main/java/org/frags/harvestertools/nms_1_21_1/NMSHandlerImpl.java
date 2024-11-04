package org.frags.harvestertools.nms_1_21_1;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.NMSHandler;

import java.util.ArrayList;
import java.util.List;

public class NMSHandlerImpl implements NMSHandler {

    private final BlockRestoreTask task;

    public NMSHandlerImpl() {
        this.task = new BlockRestoreTask();

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        scheduler.scheduleSyncRepeatingTask(HarvesterTools.getInstance(), task, 0L, 40L);
    }

    @Override
    public void replenishCrop(Block block, HarvesterTools plugin) {

        long timeToReplenish = System.currentTimeMillis() + (plugin.getConfig().getLong("tools.hoe.time-to-replenish") * 1000);

        World world = block.getWorld();
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            world.loadChunk(chunk);
            chunk.setForceLoaded(true);
            chunk.load();
        }

        if (chunk.getPluginChunkTickets().isEmpty()) {
            chunk.addPluginChunkTicket(plugin);
        }

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());

        int x = block.getX() & 15;
        int y = block.getY() & 15;
        int z = block.getZ() & 15;

        final LevelChunk levelChunk = nmsWorld.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        levelChunk.setLoaded(true);
        final LevelChunkSection levelChunkSection = levelChunk.getSection(levelChunk.getSectionIndex(block.getY()));
        final BlockState blockState = levelChunk.getSection(levelChunk.getSectionIndex(block.getY())).getBlockState(x, y, z);

        BlockState newState;

        if (blockState.hasProperty(BlockStateProperties.AGE_3)) {
            newState = blockState.setValue(BlockStateProperties.AGE_3, 0);
        } else {
            newState = blockState.setValue(BlockStateProperties.AGE_7, 0);
        }

        levelChunkSection.setBlockState(x, y, z, newState);

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockPos, newState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();

            serverPlayer.connection.send(packet);
        }


        BlockState grownState;

        if (blockState.hasProperty(BlockStateProperties.AGE_3)) {
            grownState = blockState.setValue(BlockStateProperties.AGE_3, 3);
        } else {
            grownState = blockState.setValue(BlockStateProperties.AGE_7, 7);
        }

        task.addToQueue(new ReplenishCrop(block, blockPos, x, y, z, levelChunk, levelChunkSection, grownState, timeToReplenish, plugin));
    }

    @Override
    public List<ItemStack> getDrops(Block block) {
        World world = block.getWorld();
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerLevel serverLevel = nmsWorld.getLevel();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());

        final LevelChunk levelChunk = serverLevel.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        final BlockState blockState = levelChunk.getSection(levelChunk.getSectionIndex(block.getY())).getBlockState(block.getX() & 15, block.getY() & 15, block.getZ() & 15);

        LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                .withParameter(LootContextParams.TOOL, net.minecraft.world.item.ItemStack.EMPTY);


        List<net.minecraft.world.item.ItemStack> drops = blockState.getDrops(lootBuilder);
        List<ItemStack> ret = new ArrayList<>();

        for (net.minecraft.world.item.ItemStack item : drops) {
            ret.add(CraftItemStack.asBukkitCopy(item));
        }

        return ret;
    }

    @Override
    public void regenBlock(Block block, HarvesterTools plugin) {
        long timeToRegen = plugin.getConfig().getLong("tools.pickaxe.time-to-regen") * 20;

        World world = block.getWorld();
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());

        int x = block.getX() & 15;
        int y = block.getY() & 15;
        int z = block.getZ() & 15;

        final LevelChunk levelChunk = nmsWorld.getChunk(block.getX() >> 4, block.getZ() >> 4);
        final LevelChunkSection levelChunkSection = levelChunk.getSection(levelChunk.getSectionIndex(block.getY()));
        final BlockState blockState = levelChunk.getSection(levelChunk.getSectionIndex(block.getY())).getBlockState(x, y, z);

        final BlockState newState = Blocks.BEDROCK.defaultBlockState();

        levelChunkSection.setBlockState(x, y, z, newState);

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockPos, newState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();

            serverPlayer.connection.send(packet);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            levelChunkSection.setBlockState(x, y, z, blockState);


            ClientboundBlockUpdatePacket newPacket = new ClientboundBlockUpdatePacket(blockPos, blockState);

            for (Player player : Bukkit.getOnlinePlayers()) {
                CraftPlayer craftPlayer = (CraftPlayer) player;
                ServerPlayer serverPlayer = craftPlayer.getHandle();

                serverPlayer.connection.send(newPacket);
            }

        }, timeToRegen);
    }

}

