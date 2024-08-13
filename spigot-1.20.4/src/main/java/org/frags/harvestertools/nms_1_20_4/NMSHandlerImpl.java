package org.frags.harvestertools.nms_1_20_4;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.NMSHandler;

import java.util.ArrayList;
import java.util.List;

public class NMSHandlerImpl implements NMSHandler {


    @Override
    public void replenishCrop(org.bukkit.block.Block block, HarvesterTools plugin) {

        long timeToReplenish = plugin.getConfig().getLong("tools.hoe.time-to-replenish") * 20;

        World world = block.getWorld();
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());

        int x = block.getX() & 15;
        int y = block.getY() & 15;
        int z = block.getZ() & 15;

        final LevelChunk levelChunk = nmsWorld.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        final LevelChunkSection levelChunkSection = levelChunk.getSection(levelChunk.getSectionIndex(block.getY()));
        final BlockState blockState = levelChunk.getSection(levelChunk.getSectionIndex(block.getY())).getBlockState(x, y, z);


        BlockState newState = blockState.setValue(BlockStateProperties.AGE_7, 0);

        levelChunkSection.setBlockState(x, y, z, newState);

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockPos, newState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();

            serverPlayer.connection.send(packet);
        }


        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            levelChunkSection.setBlockState(x, y, z, blockState);

            BlockState grownState = blockState.setValue(BlockStateProperties.AGE_7, 7);

            ClientboundBlockUpdatePacket grownPacket = new ClientboundBlockUpdatePacket(blockPos, grownState);

            for (Player player : Bukkit.getOnlinePlayers()) {
                CraftPlayer craftPlayer = (CraftPlayer) player;
                ServerPlayer serverPlayer = craftPlayer.getHandle();

                serverPlayer.connection.send(grownPacket);
            }
        }, timeToReplenish);


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

}
