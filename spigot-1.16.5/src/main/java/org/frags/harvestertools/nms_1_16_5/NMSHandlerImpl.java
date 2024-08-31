package org.frags.harvestertools.nms_1_16_5;

import com.mojang.serialization.ListBuilder;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootContext;
import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.NMSHandler;

import java.util.ArrayList;
import java.util.List;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public void replenishCrop(Block block, HarvesterTools plugin) {

        long timeToReplenish = plugin.getConfig().getLong("tools.hoe.time-to-replenish") * 20;

        World world = block.getWorld();
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());

        int x = block.getX() & 15;
        int y = block.getY() & 15;
        int z = block.getZ() & 15;

        int ySection = block.getY() >> 4;


        final Chunk levelChunk = nmsWorld.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        final ChunkSection levelChunkSection = levelChunk.getSections()[ySection];
        final IBlockData blockState = levelChunkSection.getType(x,  y, z);


        IBlockData newState = blockState.set(BlockCrops.AGE, BlockCrops.AGE.min);

        levelChunkSection.setType(x, y, z, newState);

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockPos, newState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            EntityPlayer nmsPlayer = craftPlayer.getHandle();

            nmsPlayer.playerConnection.sendPacket(packet);
        }


        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            levelChunkSection.setType(x, y, z, blockState);

            IBlockData grownState = blockState.set(BlockCrops.AGE, BlockCrops.AGE.max);

            PacketPlayOutBlockChange grownPacket = new PacketPlayOutBlockChange(blockPos, grownState);

            for (Player player : Bukkit.getOnlinePlayers()) {
                CraftPlayer craftPlayer = (CraftPlayer) player;
                EntityPlayer nmsPlayer = craftPlayer.getHandle();

                nmsPlayer.playerConnection.sendPacket(grownPacket);
            }
        }, timeToReplenish);

    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getDrops(Block block) {
        World world = block.getWorld();
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        BaseBlockPosition blockPos = new BaseBlockPosition(block.getX(), block.getY(), block.getZ());

        int ySection = block.getY() >> 4;


        final Chunk levelChunk = nmsWorld.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        final ChunkSection levelChunkSection = levelChunk.getSections()[ySection];
        final IBlockData blockState = levelChunkSection.getType(block.getX() & 15, block.getY() & 15, block.getZ() & 15);


        LootTableInfo.Builder lootContext = new LootTableInfo.Builder(nmsWorld)
                .set(LootContextParameters.TOOL, ItemStack.b)
                .set(LootContextParameters.ORIGIN, Vec3D.a(blockPos));


        List<ItemStack> drops = blockState.a(lootContext);
        List<org.bukkit.inventory.ItemStack> ret = new ArrayList<>();

        for (ItemStack item : drops) {
            ret.add(CraftItemStack.asBukkitCopy(item));
        }

        return ret;
    }

    @Override
    public void regenBlock(Block block, HarvesterTools plugin) {
        long timeToRegen = plugin.getConfig().getLong("tools.pickaxe.time-to-regen") * 20;

        World world = block.getWorld();
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());

        int x = block.getX() & 15;
        int y = block.getY() & 15;
        int z = block.getZ() & 15;

        int ySection = block.getY() >> 4;


        final Chunk levelChunk = nmsWorld.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        final ChunkSection levelChunkSection = levelChunk.getSections()[ySection];
        final IBlockData blockState = levelChunkSection.getType(x,  y, z);

        final IBlockData newState = Blocks.BEDROCK.getBlockData();

        levelChunkSection.setType(x, y, z, newState);

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockPos, newState);

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            EntityPlayer nmsPlayer = craftPlayer.getHandle();

            nmsPlayer.playerConnection.sendPacket(packet);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            levelChunkSection.setType(x, y, z, blockState);

            PacketPlayOutBlockChange newPacket = new PacketPlayOutBlockChange(blockPos, blockState);

            for (Player player : Bukkit.getOnlinePlayers()) {
                CraftPlayer craftPlayer = (CraftPlayer) player;
                EntityPlayer serverPlayer = craftPlayer.getHandle();

                serverPlayer.playerConnection.sendPacket(newPacket);
            }
        }, timeToRegen);
    }
}
